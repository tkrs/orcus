package example.bigtable

import java.time.Instant

import cats.data.{Coproduct, Kleisli}
import cats.free.Free
import cats.implicits._
import cats.~>
import com.google.cloud.bigtable.hbase.BigtableConfiguration
import com.google.cloud.bigtable.hbase.BigtableOptionsFactory._
import orcus.free._
import orcus.free.handler.result.{Handler => ResultHandler}
import orcus.free.handler.resultScanner.{Handler => ResultScannerHandler}
import orcus.free.handler.table.{Handler => TableHandler}
import org.apache.hadoop.hbase.client._
import org.apache.hadoop.hbase.util.Bytes

import scala.util.Try

object FreeMain extends App {
  import Functions._

  def putProgram[F[_]](prefix: String, numRecords: Int)(
      implicit
      ev1: TableOps[F]): Free[F, Vector[(Array[Byte], Long)]] = {

    def mkPut: Put = {
      val ts     = System.currentTimeMillis()
      val rowKey = Bytes.toBytes(s"$prefix#${Long.MaxValue - ts}")

      new Put(rowKey, ts)
        .setTTL(1800)
        .addColumn(columnFamilyName,
                   columnName,
                   Bytes.toBytes(s"$greeting at ${Instant.ofEpochMilli(ts)}"))
    }

    def prog =
      for {
        _   <- Free.pure(Thread.sleep(10))
        put <- Free.pure(mkPut)
        _   <- ev1.put(put)
      } yield {
        (put.getRow, put.getTimeStamp)
      }

    Iterator.continually(prog).take(numRecords).toVector.sequence[Free[F, ?], (Array[Byte], Long)]
  }

  def scanProgram[F[_]](prefix: String, numRecords: Int, range: (Long, Long))(
      implicit
      ev1: TableOps[F],
      ev2: ResultScannerOps[F]): Free[F, Seq[Result]] = {

    def mkScan: Scan = {
      new Scan()
        .setRowPrefixFilter(Bytes.toBytes(prefix))
        .setTimeRange(range._1, range._2)
    }

    for {
      scan <- Free.pure(mkScan)
      r    <- ev1.getScanner(scan)
      xs   <- ev2.next(r, numRecords)
    } yield xs
  }

  def resultProgram[F[_]](results: Seq[Result])(
      implicit
      ev1: ResultOps[F]): Free[F, Vector[Option[String]]] = {
    for {
      ys <- results.toVector
             .map(r => ev1.get[String](r, columnFamilyName, columnName))
             .sequence[Free[F, ?], Option[String]]
    } yield ys
  }

  def program[F[_]](implicit T: TableOps[F], R: ResultOps[F], RS: ResultScannerOps[F]) = {
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

  val projectId  = sys.props("project-id")
  val instanceId = sys.props("instance-id")
  val emulator   = sys.props.contains("emulator")
  val config     = BigtableConfiguration.configure(projectId, instanceId)
  if (emulator) config.setBoolean(BIGTABLE_USE_PLAINTEXT_NEGOTIATION, true)

  type K[F[_], A] = Kleisli[F, Table, A]

  type Op1[A] = Coproduct[ResultScannerOp, ResultOp, A]
  type Op[A]  = Coproduct[TableOp, Op1, A]

  def interpreter[M[_]](
      implicit
      T: TableHandler[M],
      R: ResultHandler[M],
      RS: ResultScannerHandler[M]
  ): Op ~> Kleisli[M, Table, ?] = {
    val op1 = RS.liftF[Table] or R.liftF[Table]
    val op  = T or op1
    op
  }

  import cats.instances.try_._

  type OpK[A] = Kleisli[Try, Table, A]

  bracket(Try(BigtableConfiguration.connect(config))) { conn =>
    val t            = conn.getTable(tableName)
    val i: Op ~> OpK = interpreter[Try]
    val k            = program[Op].foldMap(i)
    k.run(t).map(_.foreach(println))
  }.get
}
