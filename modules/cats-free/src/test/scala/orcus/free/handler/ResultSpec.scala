package orcus.free.handler

import java.nio.ByteBuffer
import java.util

import cats.instances.either._
import cats.~>
import orcus.codec.auto._
import orcus.free.handler.result.Handler
import orcus.free.{ResultOp, ResultOps}
import org.apache.hadoop.hbase.Cell
import org.apache.hadoop.hbase.client.Result
import org.apache.hadoop.hbase.util.Bytes
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.{FunSpec, Matchers}
import org.scalatestplus.mockito.MockitoSugar

import scala.collection.JavaConverters._

class ResultSpec extends FunSpec with MockitoSugar with Matchers {
  type F[A] = Either[Throwable, A]

  def interpreter[M[_], A](implicit H: Handler[M]): ResultOp ~> M = H

  def ops[M[_]](implicit T: ResultOps[M]): ResultOps[M] = T

  describe("Handler") {
    describe("getRow") {
      it("should take the row successfully") {
        val m = mock[Result]

        val row  = "1"
        val _row = Bytes.toBytes(row)
        when(m.getRow).thenReturn(_row)

        val Right(Some(v)) = ops[ResultOp]
          .getRow(m)
          .foldMap(interpreter[F, Array[Byte]])

        assert(Bytes.toString(v) === row)
      }
    }
    describe("rawCells") {
      it("should take the cells successfully") {
        val m = mock[Result]

        val cells = Iterator.continually(mock[Cell]).take(10).toSeq

        when(m.rawCells()).thenReturn(cells.toArray[Cell])

        val Right(v) = ops[ResultOp]
          .rawCells(m)
          .foldMap(interpreter[F, Seq[Cell]])

        assert(v === cells)
      }
    }
    describe("getColumnCells") {
      it("should take the cells successfully") {
        val m = mock[Result]

        val family     = "1"
        val qualifier  = "2"
        val cells      = Iterator.continually(mock[Cell]).take(10).toSeq
        val _family    = Bytes.toBytes(family)
        val _qualifier = Bytes.toBytes(qualifier)

        when(m.getColumnCells(_family, _qualifier)).thenReturn(cells.asJava)

        val Right(v) = ops[ResultOp]
          .getColumnCells(m, _family, _qualifier)
          .foldMap(interpreter[F, Seq[Cell]])

        assert(v === cells)
      }
    }
    describe("getColumnLatestCell") {
      it("should take the cell successfully") {
        val m = mock[Result]

        val family     = "1"
        val qualifier  = "2"
        val cell       = mock[Cell]
        val _family    = Bytes.toBytes(family)
        val _qualifier = Bytes.toBytes(qualifier)

        when(m.getColumnLatestCell(_family, _qualifier)).thenReturn(cell)

        val Right(Some(v)) = ops[ResultOp]
          .getColumnLatestCell(m, _family, _qualifier)
          .foldMap(interpreter[F, Cell])

        assert(v === cell)
      }
    }
    describe("get") {
      it("should take the value successfully") {
        val m = mock[Result]

        val family     = "1"
        val qualifier  = "2"
        val value      = "3"
        val _family    = Bytes.toBytes(family)
        val _qualifier = Bytes.toBytes(qualifier)
        val _value     = Bytes.toBytes(value)

        when(m.getValue(_family, _qualifier)).thenReturn(_value)

        val Right(Some(v)) = ops[ResultOp]
          .get[String](m, _family, _qualifier)
          .foldMap(interpreter[F, String])

        assert(v === value)
      }
    }
    describe("getValue") {
      it("should take the value successfully") {
        val m = mock[Result]

        val family     = "1"
        val qualifier  = "2"
        val value      = "3"
        val _family    = Bytes.toBytes(family)
        val _qualifier = Bytes.toBytes(qualifier)
        val _value     = Bytes.toBytes(value)

        when(m.getValue(_family, _qualifier)).thenReturn(_value)

        val Right(Some(v)) = ops[ResultOp]
          .getValue(m, _family, _qualifier)
          .foldMap(interpreter[F, Array[Byte]])

        assert(Bytes.toString(v) === value)
      }
    }
    describe("getValueAsByteBuffer") {
      it("should take the value successfully") {
        val m = mock[Result]

        val family     = "1"
        val qualifier  = "2"
        val value      = "3"
        val _family    = Bytes.toBytes(family)
        val _qualifier = Bytes.toBytes(qualifier)
        val _value     = ByteBuffer.wrap(Bytes.toBytes(value))

        when(m.getValueAsByteBuffer(_family, _qualifier)).thenReturn(_value)

        val Right(Some(v)) = ops[ResultOp]
          .getValueAsByteBuffer(m, _family, _qualifier)
          .foldMap(interpreter[F, ByteBuffer])

        assert(Bytes.toString(v.array()) === value)
      }
    }
    describe("getFamilyMap") {
      it("should take the map successfully") {
        val m = mock[Result]

        val family  = "1"
        val value   = new util.TreeMap[Array[Byte], Array[Byte]]
        val _family = Bytes.toBytes(family)

        when(m.getFamilyMap(_family)).thenReturn(value)

        val Right(v) = ops[ResultOp]
          .getFamilyMap(m, _family)
          .foldMap(interpreter[F, Map[Array[Byte], Array[Byte]]])

        assert(v === value.asScala.toMap)
      }
    }
    describe("getFamily") {
      it("should take the value successfully") {
        case class Foo(x: Option[Int], y: Option[Long])
        val m = mock[Result]

        val family  = "1"
        val value   = new util.TreeMap[Array[Byte], Array[Byte]](Bytes.BYTES_COMPARATOR)
        val _family = Bytes.toBytes(family)

        value.put(Bytes.toBytes("x"), Bytes.toBytes(1: Int))

        when(m.getFamilyMap(_family)).thenReturn(value)

        val Right(v) = ops[ResultOp]
          .getFamily[Foo](m, _family)
          .foldMap(interpreter[F, Foo])

        assert(v === Foo(x = Some(1), None))
      }
    }
    describe("to") {
      it("should take the value successfully") {
        case class Bar(x: Option[String], y: Option[Int])
        case class Foo(a: Option[Bar])

        val m = mock[Result]

        val value = new util.TreeMap[Array[Byte], Array[Byte]](Bytes.BYTES_COMPARATOR)

        value.put(Bytes.toBytes("x"), Bytes.toBytes("*"))

        when(m.getFamilyMap(any[Array[Byte]])).thenReturn(value)

        val Right(v) = ops[ResultOp]
          .to[Foo](m)
          .foldMap(interpreter[F, Foo])

        assert(v === Foo(Some(Bar(Some("*"), None))))
      }
    }
  }
}
