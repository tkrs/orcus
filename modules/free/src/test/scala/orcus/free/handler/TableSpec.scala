package orcus.free.handler

import java.{lang => jl}
import java.util.concurrent.CompletableFuture

import cats.~>
import cats.data.Kleisli
import cats.instances.future._
import orcus.{BatchResult, table => ot}
import orcus.free.{TableOp, TableOps}
import orcus.free.handler.table.Handler
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.TableName
import org.apache.hadoop.hbase.client.{
  AsyncTable,
  Result,
  ResultScanner,
  RowMutations,
  ScanResultConsumer,
  Append => HAppend,
  Delete => HDelete,
  Get => HGet,
  Increment => HIncrement,
  Put => HPut,
  Scan => HScan
}
import org.apache.hadoop.hbase.util.Bytes
import org.scalatest.{FunSpec, Matchers}
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito._

import scala.collection.JavaConverters._
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class TableSpec extends FunSpec with MockitoSugar with Matchers {

  type F[A] = Future[A]

  def interpreter[M[_], A](implicit H: Handler[M]): TableOp ~> Kleisli[M, ot.AsyncTableT, ?] = H

  def ops[M[_]](implicit T: TableOps[M]): TableOps[M] = T

  describe("Handler") {
    describe("getName") {
      it("should take the table name successfully") {
        val m = mock[AsyncTable[ScanResultConsumer]]

        val tn = TableName.valueOf("1")

        when(m.getName).thenReturn(tn)

        val f = ops[TableOp].getName.foldMap(interpreter[F, TableName]).run(m)
        val v = Await.result(f, 3.seconds)

        assert(tn === v)
      }
    }

    describe("getConfiguration") {
      it("should take the configuration successfully") {
        val m = mock[AsyncTable[ScanResultConsumer]]

        val c = new Configuration(false)

        when(m.getConfiguration).thenReturn(c)

        val f = ops[TableOp].getConfiguration.foldMap(interpreter[F, Configuration]).run(m)
        val v = Await.result(f, 3.seconds)

        assert(c === v)
      }
    }

    describe("exists") {
      it("should take the existence successfully") {
        val m = mock[AsyncTable[ScanResultConsumer]]

        val g = new HGet(Bytes.toBytes("1"))

        when(m.exists(g)).thenReturn(CompletableFuture.completedFuture(jl.Boolean.TRUE))

        val f = ops[TableOp].exists(g).foldMap(interpreter[F, Boolean]).run(m)
        val v = Await.result(f, 3.seconds)

        assert(v)
      }
    }

    describe("get") {
      it("should take the result successfully") {
        val m = mock[AsyncTable[ScanResultConsumer]]
        val r = mock[Result]

        val g = new HGet(Bytes.toBytes("1"))

        when(m.get(g)).thenReturn(CompletableFuture.completedFuture(r))

        val f = ops[TableOp].get(g).foldMap(interpreter[F, Result]).run(m)
        val v = Await.result(f, 3.seconds)

        assert(r === v)
      }
    }

    describe("put") {
      it("should put the row successfully") {
        val m = mock[AsyncTable[ScanResultConsumer]]

        val g = new HPut(Bytes.toBytes("1"))

        when(m.put(g)).thenReturn(CompletableFuture.completedFuture(null.asInstanceOf[Void]))

        val f = ops[TableOp].put(g).foldMap(interpreter[F, Unit]).run(m)
        val v = Await.result(f, 3.seconds)

        assert(v.isInstanceOf[Unit])
      }
    }

    describe("scanAll") {
      it("should call scanAll successfully") {
        val m = mock[AsyncTable[ScanResultConsumer]]

        val scan = new HScan()
        val expected = Seq(
          mock[Result],
          mock[Result]
        )

        when(m.scanAll(scan)).thenReturn(CompletableFuture.completedFuture(expected.asJava))

        val f = ops[TableOp].scanAll(scan).foldMap(interpreter[F, Unit]).run(m)
        val v = Await.result(f, 3.seconds)

        assert(v === expected)
      }
    }

    describe("getScanner") {
      it("should take the scan result successfully") {
        val m = mock[AsyncTable[ScanResultConsumer]]
        val r = mock[ResultScanner]

        val s = new HScan()
          .withStartRow(Bytes.toBytes("1"))

        when(m.getScanner(s)).thenReturn(r)

        val f = ops[TableOp].getScanner(s).foldMap(interpreter[F, ResultScanner]).run(m)
        val v = Await.result(f, 3.seconds)

        assert(r === v)
      }
    }

    describe("delete") {
      it("should delete the row successfully") {
        val m = mock[AsyncTable[ScanResultConsumer]]

        val d = new HDelete(Bytes.toBytes("1"))

        when(m.delete(d)).thenReturn(CompletableFuture.completedFuture(null.asInstanceOf[Void]))

        val f = ops[TableOp].delete(d).foldMap(interpreter[F, Unit]).run(m)
        val v = Await.result(f, 3.seconds)

        assert(v.isInstanceOf[Unit])
      }
    }

    describe("append") {
      it("should append the row successfully") {
        val m = mock[AsyncTable[ScanResultConsumer]]
        val r = mock[Result]

        val a = new HAppend(Bytes.toBytes("1"))

        when(m.append(a)).thenReturn(CompletableFuture.completedFuture(r))

        val f = ops[TableOp].append(a).foldMap(interpreter[F, Result]).run(m)
        val v = Await.result(f, 3.seconds)

        assert(r === v)
      }
    }

    describe("increment") {
      it("should take the row successfully") {
        val m = mock[AsyncTable[ScanResultConsumer]]
        val r = mock[Result]

        val a = new HIncrement(Bytes.toBytes("1"))

        when(m.increment(a)).thenReturn(CompletableFuture.completedFuture(r))

        val f = ops[TableOp].increment(a).foldMap(interpreter[F, Result]).run(m)
        val v = Await.result(f, 3.seconds)

        assert(r === v)
      }
    }

    describe("batch") {
      it("should take the row successfully") {
        val m = mock[AsyncTable[ScanResultConsumer]]

        val a = Seq(
          new HIncrement(Bytes.toBytes("1")),
          new HDelete(Bytes.toBytes("2")),
          new HGet(Bytes.toBytes("3")),
          new HGet(Bytes.toBytes("error")),
          new HAppend(Bytes.toBytes("1")),
          new HPut(Bytes.toBytes("3")),
          new HIncrement(Bytes.toBytes("4")),
          new RowMutations(Bytes.toBytes("s")),
          new HGet(Bytes.toBytes("error"))
        )
        val r1 = mock[Result]
        val r2 = mock[Result]
        val r3 = mock[Result]
        val r4 = mock[Result]
        val ex = new Exception("Oops")
        val returns: Seq[Object] = Seq(
          r1,
          null.asInstanceOf[Void],
          null.asInstanceOf[Result],
          ex,
          r2,
          null.asInstanceOf[Void],
          r3,
          r4,
          ""
        )
        val expected = Vector(
          BatchResult.Mutate(Some(r1)),
          BatchResult.VoidMutate,
          BatchResult.Mutate(None),
          BatchResult.Error(ex, new HGet(Bytes.toBytes("error"))),
          BatchResult.Mutate(Some(r2)),
          BatchResult.VoidMutate,
          BatchResult.Mutate(Some(r3)),
          BatchResult.Mutate(Some(r4)),
          BatchResult.Error(new Exception("Unexpected class returned: String"),
                            new HGet(Bytes.toBytes("error")))
        )

        when(m.batch[Object](a.asJava))
          .thenReturn(returns.map {
            case r: Exception =>
              val cf = new CompletableFuture[Object]
              cf.completeExceptionally(r)
              cf
            case r =>
              CompletableFuture.completedFuture(r)
          }.asJava)

        val f =
          ops[TableOp].batch[Vector](a).foldMap(interpreter[F, Vector[Option[HIncrement]]]).run(m)
        val v = Await.result(f, 3.seconds)

        assert(v === expected)
      }
    }
  }
}
