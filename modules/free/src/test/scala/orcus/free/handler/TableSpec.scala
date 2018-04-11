package orcus.free.handler

import java.{lang => jl}
import java.util.concurrent.CompletableFuture

import cats.~>
import cats.data.Kleisli
import cats.instances.future._
import orcus.{table => ot}
import orcus.free.{TableOp, TableOps}
import orcus.free.handler.table.Handler
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.TableName
import org.apache.hadoop.hbase.client.{
  AsyncTable,
  Result,
  ResultScanner,
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

    describe("batchS") {
      it("should take the row successfully") {
        val m = mock[AsyncTable[ScanResultConsumer]]

        val a = Seq(
          new HIncrement(Bytes.toBytes("1")),
          new HIncrement(Bytes.toBytes("2")),
          new HIncrement(Bytes.toBytes("3")),
          new HIncrement(Bytes.toBytes("4"))
        )
        val returns = Seq(
          mock[Result],
          mock[Result],
          mock[Result],
          mock[Result]
        ).toVector
        val expected = returns.map(Option.apply)

        when(m.batch[Result](a.asJava))
          .thenReturn(returns.map(r => CompletableFuture.completedFuture(r)).asJava)

        val f = ops[TableOp].batchS(a).foldMap(interpreter[F, Vector[Option[HIncrement]]]).run(m)
        val v = Await.result(f, 3.seconds)

        assert(v === expected)
      }
    }

    describe("batchT") {
      it("should take the row successfully") {
        val m = mock[AsyncTable[ScanResultConsumer]]

        val a = Seq(
          new HIncrement(Bytes.toBytes("1")),
          new HIncrement(Bytes.toBytes("2")),
          new HIncrement(Bytes.toBytes("3")),
          new HIncrement(Bytes.toBytes("4"))
        )
        val returns = Seq(
          mock[Result],
          mock[Result],
          mock[Result],
          mock[Result]
        ).toVector
        val expected = returns.map(Option.apply)

        when(m.batch[Result](a.asJava))
          .thenReturn(returns.map(r => CompletableFuture.completedFuture(r)).asJava)

        val f = ops[TableOp].batchT(a).foldMap(interpreter[F, Vector[Option[Result]]]).run(m)
        val v = Await.result(f, 3.seconds)

        assert(v === expected)
      }
    }
  }
}
