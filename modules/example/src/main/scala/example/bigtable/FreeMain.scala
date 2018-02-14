package example.bigtable

import java.time.Instant

import cats.data.{EitherK, Kleisli}
import cats.free.Free
import cats.implicits._
import cats.~>
import orcus.codec.PutEncoder
import orcus.free._
import orcus.free.handler.result.{Handler => ResultHandler}
import orcus.free.handler.resultScanner.{Handler => ResultScannerHandler}
import orcus.free.handler.table.{Handler => TableHandler}
import org.apache.hadoop.hbase.client._
import org.apache.hadoop.hbase.util.Bytes

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

final case class CF1(greeting1: Option[String], greeting2: Option[String])
final case class Hello(cf1: CF1)

object FreeMain extends App {
  import Setup._
  import Functions._

  def putProgram[F[_]](prefix: String, numRecords: Int)(
      implicit
      ev1: TableOps[F]): Free[F, Vector[(Array[Byte], Long)]] = {

    def mkPut = {
      val ts     = System.currentTimeMillis()
      val rowKey = Bytes.toBytes(s"$prefix#${Long.MaxValue - ts}")
      val hello  = Hello(CF1(Some(s"$greeting at ${Instant.ofEpochMilli(ts)}"), None))
      PutEncoder[Hello]
        .apply(new Put(rowKey, ts), hello, Long.MaxValue)
        .setTTL(1800)
        .setDurability(Durability.ASYNC_WAL)
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
      new Scan()
        .setRowPrefixFilter(Bytes.toBytes(prefix))
        .setTimeRange(range._1, range._2)

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

  type K[F[_], A] = Kleisli[F, orcus.table.AsyncTableT, A]

  type Op1[A] = EitherK[ResultScannerOp, ResultOp, A]
  type Op[A]  = EitherK[TableOp, Op1, A]

  def interpreter[M[_]](
      implicit
      T: TableHandler[M],
      R: ResultHandler[M],
      RS: ResultScannerHandler[M]
  ): Op ~> Kleisli[M, orcus.table.AsyncTableT, ?] =
    T or (RS.liftF[orcus.table.AsyncTableT] or R.liftF[orcus.table.AsyncTableT])

  type OpK[A] = Kleisli[Future, orcus.table.AsyncTableT, A]

  val resource = Future(ConnectionFactory.createAsyncConnection().join())

  val f = bracket(resource) { conn =>
    val t            = conn.getTable(tableName)
    val i: Op ~> OpK = interpreter[Future]
    val k            = program[Op].foldMap(i)
    k.run(t).map(_.foreach(println))
  }

  Await.result(f, 10.seconds)
}
