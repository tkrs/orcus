package example

import java.time.LocalDate
import java.util.concurrent.CompletableFuture

import cats.data.EitherK
import cats.data.Kleisli
import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.free.Free
import cats.implicits._
import cats.~>
import com.google.cloud.bigtable.hbase.BigtableConfiguration
import com.typesafe.scalalogging.LazyLogging
import orcus.admin
import orcus.async._
import orcus.async.implicits._
import orcus.async.instances.catsEffect.concurrent._
import orcus.codec._
import orcus.codec.semiauto._
import orcus.free._
import orcus.free.handler.result.{Handler => ResultHandler}
import orcus.free.handler.resultScanner.{Handler => ResultScannerHandler}
import orcus.free.handler.table.{Handler => TableHandler}
import orcus.table.AsyncTableT
import org.apache.hadoop.hbase.TableName
import org.apache.hadoop.hbase.client._
import org.apache.hadoop.hbase.util.Bytes

import scala.concurrent.duration._

trait FreeMain extends IOApp with LazyLogging {
  import Syntax._

  final val tableName: TableName = TableName.valueOf("novelist")
  final val keyPrefix: String    = "novel"

  final val novels = Map(
    "Ryunosuke Akutagawa" -> List(
      Novel(Work("Rashomon", LocalDate.of(1915, 11, 1))),
      Novel(Work("Hana", LocalDate.of(1916, 1, 1))),
      Novel(Work("In a Grove", LocalDate.of(1922, 1, 1)))
    ),
    "Soseki Natsume" -> List(
      Novel(Work("Kokoro", LocalDate.of(1914, 4, 20))),
      Novel(Work("I Am a Cat", LocalDate.of(1905, 1, 1))),
      Novel(Work("Botchan", LocalDate.of(1906, 1, 1)))
    )
  )

  final def createTable(conn: AsyncAdmin): IO[Unit] = {
    val tableDescripter = TableDescriptorBuilder
      .newBuilder(tableName)
      .setColumnFamily(ColumnFamilyDescriptorBuilder.newBuilder(Bytes.toBytes("work")).build())
      .build()

    admin.tableExists[IO](conn, tableName).ifM(IO.unit, admin.createTable[IO](conn, tableDescripter))
  }

  final def deleteTable(conn: AsyncAdmin): IO[Unit] =
    admin.deleteTable[IO](conn, tableName)

  final def putProgram[F[a]](implicit
    tableOps: TableOps[F]
  ): Free[F, Unit] = {
    def mkPut(author: String, novel: Novel) = {
      val ts     = System.currentTimeMillis()
      val rowKey = novel.work.key(keyPrefix, author)

      PutEncoder[Novel]
        .apply(new Put(rowKey, ts), novel)
        .setTTL(1800)
        .setDurability(Durability.ASYNC_WAL)
    }

    def prog(author: String, novels: List[Novel]): Free[F, Unit] =
      novels
        .traverse(n => Free.pure[F, Put](mkPut(author, n)) >>= (p => tableOps.put(p))) *> Free.pure(())

    novels.toList
      .map((prog _).tupled)
      .sequence[Free[F, ?], Unit] *> Free.pure(())
  }

  final def scanProgram[F[_]](numRecords: Int)(implicit
    tableOps: TableOps[F],
    resultScannerOps: ResultScannerOps[F]
  ): Free[F, Seq[Result]] = {
    def mkScan =
      new Scan()
        .setRowPrefixFilter(Bytes.toBytes(keyPrefix))

    tableOps.getScanner(mkScan) >>= (sc => resultScannerOps.next(sc, numRecords))
  }

  final def resultProgram[F[_]](results: Seq[Result])(implicit
    resultOps: ResultOps[F]
  ): Free[F, Vector[Option[Novel]]] =
    results.toVector
      .map(resultOps.to[Option[Novel]])
      .sequence[Free[F, ?], Option[Novel]]

  final def program[F[_]](implicit
    T: TableOps[F],
    R: ResultOps[F],
    RS: ResultScannerOps[F]
  ): Free[F, Vector[Option[Novel]]] = {
    val numRecords = 100

    for {
      _  <- putProgram[F]
      xs <- scanProgram[F](numRecords)
      ys <- resultProgram(xs)
    } yield ys
  }

  final type Algebra[A]      = EitherK[TableOp, EitherK[ResultOp, ResultScannerOp, ?], A]
  final type TableK[F[_], A] = Kleisli[F, AsyncTableT, A]

  final def interpreter[M[_]](implicit
    tableHandlerM: TableHandler[M],
    resultHandlerM: ResultHandler[M],
    resultScannerHandlerM: ResultScannerHandler[M]
  ): Algebra ~> TableK[M, ?] = {
    val t: TableOp ~> TableK[M, ?]          = tableHandlerM
    val r: ResultOp ~> TableK[M, ?]         = resultHandlerM.liftF
    val rs: ResultScannerOp ~> TableK[M, ?] = resultScannerHandlerM.liftF

    t.or(r.or(rs))
  }

  final def runExample(conn: AsyncConnection): IO[ExitCode] = {
    val i: Algebra ~> TableK[IO, ?]          = interpreter[IO]
    val k: TableK[IO, Vector[Option[Novel]]] = program[Algebra].foldMap(i)

    for {
      a <- IO(conn.getAdmin)
      _ <- createTable(a)
      _ <- IO.sleep(3.seconds)
      t <- IO(conn.getTableBuilder(tableName).build())
      _ <- k.run(t).map(_.foreach(a => logger.info(a.toString)))
      _ <- deleteTable(a)
    } yield ExitCode.Success
  }

  final def run(args: List[String]): IO[ExitCode] =
    getConnection.bracket(runExample)(conn => IO(conn.close()))

  def getConnection: IO[AsyncConnection]
}

object HBaseMain extends FreeMain {
  def getConnection: IO[AsyncConnection] =
    Par[CompletableFuture, IO].parallel(ConnectionFactory.createAsyncConnection())
}

object BigtableMain extends FreeMain {
  def getConnection: IO[AsyncConnection] = {
    val projectId  = sys.props.getOrElse("bigtable.project-id", "fake")
    val instanceId = sys.props.getOrElse("bigtable.instance-id", "fake")
    IO(new BigtableAsyncConnection(BigtableConfiguration.configure(projectId, instanceId)))
  }
}

final case class Work(name: String, releaseDate: LocalDate)

object Work {
  implicit val releaseDateCodec: orcus.codec.ValueCodec[LocalDate] =
    ValueCodec[Long].imap(_.toEpochDay, LocalDate.ofEpochDay)

  implicit val encodeWork: PutFamilyEncoder[Work] = derivedPutFamilyEncoder[Work]
  implicit val decodeWork: FamilyDecoder[Work]    = derivedFamilyDecoder[Work]

  implicit class WorkOps(val a: Work) extends AnyVal {
    def key(prefix: String, author: String): Array[Byte] = {
      val b = new StringBuilder
      val v = b.append(prefix).append(Long.MaxValue - a.releaseDate.toEpochDay).append(author).append(a.name)
      Bytes.toBytes(v.toString())
    }
  }
}

final case class Novel(work: Work)

object Novel {
  implicit val decodeNovel: Decoder[Novel]       = derivedDecoder[Novel]
  implicit val encodePutNovel: PutEncoder[Novel] = derivedPutEncoder[Novel]
}

object Syntax {
  implicit final class Nat[F[_], G[_]](val nat: F ~> G) extends AnyVal {
    def liftF[E]: F ~> Kleisli[G, E, ?] = λ[F ~> Kleisli[G, E, ?]](fa => Kleisli(_ => nat(fa)))
  }
}
