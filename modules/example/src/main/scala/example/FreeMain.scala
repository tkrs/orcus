package example

import java.time.Instant
import java.util.function.BiConsumer

import cats.data.Kleisli
import cats.effect.IO
import cats.free.Free
import cats.implicits._
import cats.~>
import com.google.cloud.bigtable.hbase.BigtableConfiguration
import iota.{CopK, TNilK}
import iota.TListK.:::
import orcus.async.catseffect._
import orcus.codec.PutEncoder
import orcus.free.{ResultOp, ResultScannerOp, TableOp}
import orcus.free.iota._
import orcus.free.handler.result.{Handler => ResultHandler}
import orcus.free.handler.resultScanner.{Handler => ResultScannerHandler}
import orcus.free.handler.table.{Handler => TableHandler}
import orcus.table.AsyncTableT
import org.apache.hadoop.hbase.client._
import org.apache.hadoop.hbase.util.Bytes

final case class CF1(greeting1: Option[String], greeting2: Option[String])
final case class Hello(cf1: CF1)

trait FreeMain extends App {
  import Setup._
  import Functions._

  def putProgram[F[a] <: CopK[_, a]](prefix: String, numRecords: Int)(
      implicit
      ev1: TableOps[F]): Free[F, Vector[(Array[Byte], Long)]] = {

    def mkPut = {
      val ts     = System.currentTimeMillis()
      val rowKey = Bytes.toBytes(s"$prefix#${Long.MaxValue - ts}")
      val hello  = Hello(CF1(Some(s"$greeting at ${Instant.ofEpochMilli(ts)}"), None))

      PutEncoder[Hello]
        .apply(new Put(rowKey, ts), hello)
        .setTTL(1800)
        .setDurability(Durability.ASYNC_WAL)
    }

    def prog =
      for {
        _    <- Free.pure(Thread.sleep(10))
        _put <- Free.pure(mkPut)
        _    <- ev1.put(_put)
      } yield (_put.getRow, _put.getTimeStamp)

    Iterator
      .continually(prog)
      .take(numRecords)
      .toVector
      .sequence[Free[F, ?], (Array[Byte], Long)]
  }

  def scanProgram[F[a] <: CopK[_, a]](prefix: String, numRecords: Int, range: (Long, Long))(
      implicit
      ev1: TableOps[F],
      ev2: ResultScannerOps[F]): Free[F, Seq[Result]] = {

    def mkScan =
      new Scan()
        .setRowPrefixFilter(Bytes.toBytes(prefix))
        .setTimeRange(range._1, range._2)

    for {
      sc <- Free.pure(mkScan)
      r  <- ev1.getScanner(sc)
      xs <- ev2.next(r, numRecords)
    } yield xs
  }

  def resultProgram[F[a] <: CopK[_, a]](results: Seq[Result])(
      implicit
      ev1: ResultOps[F]): Free[F, Vector[Option[Hello]]] = {
    for {
      ys <- results.toVector
             .map(ev1.to[Option[Hello]])
             .sequence[Free[F, ?], Option[Hello]]
    } yield ys
  }

  def program[F[a] <: CopK[_, a]](implicit
                                  T: TableOps[F],
                                  R: ResultOps[F],
                                  RS: ResultScannerOps[F]): Free[F, Vector[Option[Hello]]] = {
    val rowKey     = "greeting"
    val numRecords = 100

    for {
      xs <- putProgram[F](rowKey, numRecords)
      h = xs.head._2
      _ = println(h)
      t = xs.last._2
      _ = println(t)
      xs <- scanProgram[F](rowKey, numRecords, (h, t))
      ys <- resultProgram(xs)
    } yield ys
  }

  type Algebra[A]      = CopK[TableOp ::: ResultOp ::: ResultScannerOp ::: TNilK, A]
  type TableK[F[_], A] = Kleisli[F, AsyncTableT, A]

  def interpreter[M[_]](
      implicit
      T: TableHandler[M],
      R: ResultHandler[M],
      RS: ResultScannerHandler[M]
  ): Algebra ~> TableK[M, ?] = {
    val t: TableOp ~> TableK[M, ?]          = T
    val r: ResultOp ~> TableK[M, ?]         = R.liftF
    val rs: ResultScannerOp ~> TableK[M, ?] = RS.liftF
    CopK.FunctionK.of[Algebra, TableK[M, ?]](t, r, rs)
  }

  def getConnection: IO[AsyncConnection]

  val f = bracket(getConnection) { conn =>
    val i: Algebra ~> TableK[IO, ?]          = interpreter[IO]
    val k: TableK[IO, Vector[Option[Hello]]] = program[Algebra].foldMap(i)
    val t: AsyncTableT                       = conn.getTableBuilder(tableName).build()
    k.run(t).map(_.foreach(println))
  }

  f.unsafeRunSync()
}

object HBaseMain extends FreeMain {
  def getConnection: IO[AsyncConnection] = IO.async[AsyncConnection] { cb =>
    val _ = ConnectionFactory
      .createAsyncConnection()
      .whenComplete(new BiConsumer[AsyncConnection, Throwable] {
        def accept(t: AsyncConnection, u: Throwable): Unit = {
          val _ = if (u != null) cb(u.asLeft) else cb(t.asRight)
        }
      })
  }
}

object BigtableMain extends FreeMain {
  def getConnection: IO[AsyncConnection] = {
    val projectId  = sys.props.getOrElse("bigtable.project-id", "fake")
    val instanceId = sys.props.getOrElse("bigtable.instance-id", "fake")
    val c          = BigtableConfiguration.configure(projectId, instanceId)
    IO(new BigtableAsyncConnection(c))
  }
}
