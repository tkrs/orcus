package orcus

import java.util.concurrent.CompletableFuture
import java.{lang => jl}

import cats.instances.future._
import orcus.async.implicits._
import orcus.async.instances.future._
import orcus.internal.Utils
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.TableName
import org.apache.hadoop.hbase.client._
import org.apache.hadoop.hbase.util.Bytes
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class TableSpec extends AnyFunSpec with MockitoSugar with Matchers {
  describe("getName") {
    it("should return table name") {
      val m = mock[AsyncTable[ScanResultConsumer]]
      val n = "1"
      val q = Bytes.toBytes(n)
      val t = TableName.valueOf(q)
      when(m.getName).thenReturn(t)

      val v = Await.result(table.getName[Future](m), 3.seconds)
      assert(t === v)
      verify(m).getName
    }
  }

  describe("getConfiguration") {
    it("should return configuration") {
      val m = mock[AsyncTable[ScanResultConsumer]]
      val c = new Configuration(false)
      when(m.getConfiguration).thenReturn(c)

      val v = Await.result(table.getConfiguration[Future](m), 3.seconds)
      assert(c === v)
      verify(m).getConfiguration
    }
  }

  describe("exists") {
    it("should return value obtained from exists(Get) as-is") {
      val m = mock[AsyncTable[ScanResultConsumer]]
      val n = "1"
      val rk = Bytes.toBytes(n)
      val get = new Get(rk)

      when(m.exists(get)).thenReturn(CompletableFuture.completedFuture(jl.Boolean.TRUE))

      val v = Await.result(table.exists[Future](m, get), 3.seconds)
      assert(true === v)
      verify(m).exists(get)
    }
  }

  describe("get") {
    it("should return value obtained from get(Get) as-is") {
      val m = mock[AsyncTable[ScanResultConsumer]]
      val n = "1"
      val rk = Bytes.toBytes(n)
      val get = new Get(rk)
      val res = mock[Result]

      when(m.get(get)).thenReturn(CompletableFuture.completedFuture(res))

      val v = Await.result(table.get[Future](m, get), 3.seconds)
      assert(res === v)
      verify(m).get(get)
    }
  }

  describe("put") {
    it("should return Unit obtained from put(Put) as-is") {
      val m = mock[AsyncTable[ScanResultConsumer]]
      val n = "1"
      val rk = Bytes.toBytes(n)
      val put = new Put(rk)

      when(m.put(put)).thenReturn(CompletableFuture.completedFuture(null.asInstanceOf[Void]))

      val v = Await.result(table.put[Future](m, put), 3.seconds)
      assert(v.isInstanceOf[Unit])
      verify(m).put(put)
    }
  }

  describe("scanAll") {
    it("should return Unit obtained from scanAll(Scan) as-is") {
      val m = mock[AsyncTable[ScanResultConsumer]]
      val scan = new Scan()
      val expected = Seq(
        mock[Result],
        mock[Result]
      )

      when(m.scanAll(scan)).thenReturn(CompletableFuture.completedFuture(Utils.toJavaList(expected)))

      val v = Await.result(table.scanAll[Future](m, scan), 3.seconds)
      assert(v === expected)
      verify(m).scanAll(scan)
    }
  }

  describe("getScanner") {
    it("should return ResultScanner obtained from getScanner(Scan) as-is") {
      val m = mock[AsyncTable[ScanResultConsumer]]
      val n = "1"
      val rk = Bytes.toBytes(n)
      val scan = new Scan().withStartRow(rk)
      val r = mock[ResultScanner]

      when(m.getScanner(scan)).thenReturn(r)

      val v = Await.result(table.getScanner[Future](m, scan), 3.seconds)
      assert(r === v)
      verify(m).getScanner(scan)
    }
  }

  describe("delete") {
    it("should return Unit obtained from delete(Delete) as-is") {
      val m = mock[AsyncTable[ScanResultConsumer]]
      val n = "1"
      val rk = Bytes.toBytes(n)
      val delete = new Delete(rk)

      when(m.delete(delete)).thenReturn(CompletableFuture.completedFuture(null.asInstanceOf[Void]))

      val v = Await.result(table.delete[Future](m, delete), 3.seconds)
      assert(v.isInstanceOf[Unit])
      verify(m).delete(delete)
    }
  }

  describe("append") {
    it("should return Result obtained from append(Append) as-is") {
      val m = mock[AsyncTable[ScanResultConsumer]]
      val n = "1"
      val rk = Bytes.toBytes(n)
      val append = new Append(rk)
      val res = mock[Result]

      when(m.append(append)).thenReturn(CompletableFuture.completedFuture(res))

      val v = Await.result(table.append[Future](m, append), 3.seconds)
      assert(res === v)
      verify(m).append(append)
    }
  }

  describe("increment") {
    it("should return Result obtained from increment(Increment) as-is") {
      val m = mock[AsyncTable[ScanResultConsumer]]
      val n = "1"
      val rk = Bytes.toBytes(n)
      val increment = new Increment(rk)
      val res = mock[Result]

      when(m.increment(increment)).thenReturn(CompletableFuture.completedFuture(res))

      val v = Await.result(table.increment[Future](m, increment), 3.seconds)
      assert(res === v)
      verify(m).increment(increment)
    }
  }

  describe("batch") {
    it("should return obtained values as Iterator[CompletableFuture[Result]] from batch(List[Row])") {
      val m = mock[AsyncTable[ScanResultConsumer]]
      val n = "1"
      val rk = Bytes.toBytes(n)
      val rows: Seq[Row] = Seq(
        new Increment(rk),
        new Get(rk),
        new Get(rk),
        new Delete(rk)
      )
      val returns: Seq[Object] = Seq(
        mock[Result],
        mock[Result],
        null.asInstanceOf[Result],
        null.asInstanceOf[Void]
      )
      val expected: Seq[BatchResult] = Seq(
        BatchResult.Mutate(Some(returns.head.asInstanceOf[Result])),
        BatchResult.Mutate(Some(returns.tail.head.asInstanceOf[Result])),
        BatchResult.Mutate(None),
        BatchResult.VoidMutate
      )

      when(m.batch[Object](any[java.util.List[Row]]))
        .thenReturn(Utils.toJavaList(returns.map(a => CompletableFuture.completedFuture(a))))

      val v = Await.result(table.batch[Future, Seq](m, rows), 3.seconds)
      assert(v === expected)
      verify(m).batch[Object](Utils.toJavaList(rows))
    }
  }

  describe("batchAll") {
    it("should return obtained values as Vector[Option[Result]] from batchAll(List[Row])") {
      val m = mock[AsyncTable[ScanResultConsumer]]
      val n = "1"
      val rk = Bytes.toBytes(n)
      val rows: Seq[Mutation] = Seq(
        new Increment(rk),
        new Put(rk)
      )
      val returns: Seq[Result] = Seq(
        mock[Result],
        null.asInstanceOf[Result],
        mock[Result]
      )
      val expected = returns.map(Option.apply)

      when(m.batchAll[Result](any[java.util.List[Row]]))
        .thenReturn(CompletableFuture.completedFuture(Utils.toJavaList(returns)))

      val v = Await.result(table.batchAll[Future, Seq](m, rows), 3.seconds)
      assert(v === expected)
      verify(m).batchAll[Result](Utils.toJavaList(rows))
    }
  }
}
