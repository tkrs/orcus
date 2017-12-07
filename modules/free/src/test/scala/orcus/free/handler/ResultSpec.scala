package orcus.free.handler

import java.nio.ByteBuffer

import cats.~>
import cats.instances.either._
import orcus.free.{ResultOp, ResultOps}
import orcus.free.handler.result.Handler
import org.apache.hadoop.hbase.client.Result
import org.apache.hadoop.hbase.util.Bytes
import org.scalatest.{FunSpec, Matchers}
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito._

class ResultSpec extends FunSpec with MockitoSugar with Matchers {

  type F[A] = Either[Throwable, A]

  def interpreter[M[_], A](implicit H: Handler[M]): ResultOp ~> M = H

  def ops[M[_]](implicit T: ResultOps[M]): ResultOps[M] = T

  describe("Handler") {
    describe("getValue") {
      it("should return value successfully") {
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
      it("should return value successfully") {
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
  }
}
