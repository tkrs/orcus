package orcus.free.handler

import cats.~>
import cats.data.Kleisli
import cats.instances.either._
import orcus.free.{TableOp, TableOps}
import orcus.free.handler.table.Handler
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.{HTableDescriptor, TableName}
import org.apache.hadoop.hbase.client.{
  Result,
  ResultScanner,
  Table,
  Delete => HDelete,
  Get => HGet,
  Put => HPut,
  Scan => HScan,
  Append => HAppend,
  Increment => HIncrement
}
import org.apache.hadoop.hbase.util.Bytes
import org.scalatest.{FunSpec, Matchers}
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito._

class TableSpec extends FunSpec with MockitoSugar with Matchers {

  type F[A] = Either[Throwable, A]

  def interpreter[M[_], A](implicit H: Handler[M]): TableOp ~> Kleisli[M, Table, ?] = H

  def ops[M[_]](implicit T: TableOps[M]): TableOps[M] = T

  describe("Handler") {
    describe("getName") {
      it("should return table name successfully") {
        val m = mock[Table]

        val tn = TableName.valueOf("1")

        when(m.getName).thenReturn(tn)

        val Right(v) = ops[TableOp].getName.foldMap(interpreter[F, TableName]).run(m)

        assert(v === tn)
      }
    }

    describe("getConfiguration") {
      it("should return configuration successfully") {
        val m = mock[Table]

        val c = new Configuration(false)

        when(m.getConfiguration).thenReturn(c)

        val Right(v) = ops[TableOp].getConfiguration.foldMap(interpreter[F, Configuration]).run(m)

        assert(v === c)
      }
    }

    describe("getHTableDescriptor") {
      it("should return table descriptor successfully") {
        val m = mock[Table]

        val d = new HTableDescriptor(TableName.valueOf("1"))

        when(m.getTableDescriptor).thenReturn(d)

        val Right(v) = ops[TableOp].getTableDescriptor.foldMap(interpreter[F, Configuration]).run(m)

        assert(v === d)
      }
    }

    describe("exists") {
      it("should return existence successfully") {
        val m = mock[Table]

        val g = new HGet(Bytes.toBytes("1"))

        when(m.exists(g)).thenReturn(true)

        val Right(v) = ops[TableOp].exists(g).foldMap(interpreter[F, Boolean]).run(m)

        assert(v)
      }
    }

    describe("get") {
      it("should return value successfully") {
        val m = mock[Table]
        val r = mock[Result]

        val g = new HGet(Bytes.toBytes("1"))

        when(m.get(g)).thenReturn(r)

        val Right(v) = ops[TableOp].get(g).foldMap(interpreter[F, Result]).run(m)

        assert(v === r)
      }
    }

    describe("put") {
      it("should put successfully") {
        val m = mock[Table]

        val g = new HPut(Bytes.toBytes("1"))

        doNothing().when(m).put(g)

        val Right(v) = ops[TableOp].put(g).foldMap(interpreter[F, Unit]).run(m)

        assert(v.isInstanceOf[Unit])
      }
    }

    describe("getScanner") {
      it("should return result of the scanner successfully") {
        val m = mock[Table]
        val r = mock[ResultScanner]

        val s = new HScan(Bytes.toBytes("1"))

        when(m.getScanner(s)).thenReturn(r)

        val Right(v) = ops[TableOp].getScanner(s).foldMap(interpreter[F, ResultScanner]).run(m)

        assert(v === r)
      }
    }

    describe("delete") {
      it("should delete successfully") {
        val m = mock[Table]

        val d = new HDelete(Bytes.toBytes("1"))

        doNothing().when(m).delete(d)

        val Right(v) = ops[TableOp].delete(d).foldMap(interpreter[F, Unit]).run(m)

        assert(v.isInstanceOf[Unit])
      }
    }

    describe("append") {
      it("should return result successfully") {
        val m = mock[Table]
        val r = mock[Result]

        val a = new HAppend(Bytes.toBytes("1"))

        when(m.append(a)).thenReturn(r)

        val Right(v) = ops[TableOp].append(a).foldMap(interpreter[F, Result]).run(m)

        assert(v === r)
      }
    }

    describe("increment") {
      it("should return result successfully") {
        val m = mock[Table]
        val r = mock[Result]

        val a = new HIncrement(Bytes.toBytes("1"))

        when(m.increment(a)).thenReturn(r)

        val Right(v) = ops[TableOp].increment(a).foldMap(interpreter[F, Result]).run(m)

        assert(v === r)
      }
    }

    describe("close") {
      it("should return result successfully") {
        val m = mock[Table]

        doNothing().when(m).close()

        val Right(v) = ops[TableOp].close().foldMap(interpreter[F, Result]).run(m)

        assert(v.isInstanceOf[Unit])
      }
    }
  }
}
