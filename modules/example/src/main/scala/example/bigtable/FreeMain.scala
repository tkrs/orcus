package example.bigtable

import java.time.Instant

import cats.data.{Coproduct, Kleisli}
import cats.free.Free
import cats.implicits._
import cats.~>
import com.google.cloud.bigtable.hbase.BigtableConfiguration
import orcus.builder._
import orcus.codec.PutEncoder
import orcus.free._
import orcus.free.handler.result.{Handler => ResultHandler}
import orcus.free.handler.resultScanner.{Handler => ResultScannerHandler}
import orcus.free.handler.table.{Handler => TableHandler}
import org.apache.hadoop.hbase.client._
import org.apache.hadoop.hbase.util.Bytes

import scala.util.Try

final case class CF1(greeting1: Option[String], greeting2: Option[String])
final case class Hello(cf1: CF1)

object FreeMain extends App {
  import setup._
  import Functions._

  def putProgram[F[_]](prefix: String, numRecords: Int)(
      implicit
      ev1: TableOps[F]): Free[F, Vector[(Array[Byte], Long)]] = {

    def mkPut = {
      val ts     = System.currentTimeMillis()
      val rowKey = Bytes.toBytes(s"$prefix#${Long.MaxValue - ts}")
      val hello  = Hello(CF1(Some(s"$greeting at ${Instant.ofEpochMilli(ts)}"), None))
      val put = for {
        o <- HPut.withTTL(1800)
        _ <- HPut.withDurability(Durability.ASYNC_WAL)
      } yield o

      val x = PutEncoder[Hello].apply(new Put(rowKey, ts), hello, Long.MaxValue)
      put.run(x)
    }

    def prog =
      for {
        _    <- Free.pure(Thread.sleep(10))
        _put <- Free.pure(mkPut)
        _    <- ev1.put(_put)
      } yield {
        (_put.getRow, _put.getTimeStamp)
      }

    Iterator
      .continually(prog)
      .take(numRecords)
      .toVector
      .sequence[Free[F, ?], (Array[Byte], Long)]
  }

  def scanProgram[F[_]](prefix: String, numRecords: Int, range: (Long, Long))(
      implicit
      ev1: TableOps[F],
      ev2: ResultScannerOps[F]): Free[F, Seq[Result]] = {

    def mkScan =
      (for {
        o <- HScan.withRowPrefixFilter(Bytes.toBytes(prefix))
        _ <- HScan.withTimeRange(range._1, range._2)
      } yield o).run(new Scan())

    for {
      sc <- Free.pure(mkScan)
      r  <- ev1.getScanner(sc)
      xs <- ev2.next(r, numRecords)
    } yield xs
  }

  def resultProgram[F[_]](results: Seq[Result])(
      implicit
      ev1: ResultOps[F]): Free[F, Vector[Option[Hello]]] = {
    for {
      ys <- results.toVector
             .map(ev1.to[Option[Hello]])
             .sequence[Free[F, ?], Option[Hello]]
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

  type K[F[_], A] = Kleisli[F, Table, A]

  type Op1[A] = Coproduct[ResultScannerOp, ResultOp, A]
  type Op[A]  = Coproduct[TableOp, Op1, A]

  def interpreter[M[_]](
      implicit
      T: TableHandler[M],
      R: ResultHandler[M],
      RS: ResultScannerHandler[M]
  ): Op ~> Kleisli[M, Table, ?] =
    T or (RS.liftF[Table] or R.liftF[Table])

  import cats.instances.try_._

  type OpK[A] = Kleisli[Try, Table, A]

  bracket(Try(BigtableConfiguration.connect(config))) { conn =>
    val t            = conn.getTable(tableName)
    val i: Op ~> OpK = interpreter[Try]
    val k            = program[Op].foldMap(i)
    k.run(t).map(_.foreach(println))
  }.get
}
