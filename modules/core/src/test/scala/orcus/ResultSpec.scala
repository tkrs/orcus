package orcus

import java.nio.ByteBuffer

import cats.instances.either._
import org.apache.hadoop.hbase.client.Result
import org.apache.hadoop.hbase.util.Bytes
import org.scalatest._
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito._

class ResultSpec extends FunSpec with MockitoSugar with Matchers {
  type F[A] = Either[Throwable, A]

  describe("getValue") {
    it("should return value obtained from Result.getValue(Array[Byte], Array[Byte]) as-is") {
      val m   = mock[Result]
      val cfn = Bytes.toBytes("1")
      val cn  = Bytes.toBytes("2")

      val expected = "3"
      val re       = Bytes.toBytes(expected)

      when(m.getValue(cfn, cn)).thenReturn(re)

      val Right(Some(v)) = result.getValue[F](m, cfn, cn)

      assert(expected === Bytes.toString(v))
      verify(m).getValue(cfn, cn)
    }
    it("should return empty when Result.getValue(Array[Byte], Array[Byte]) returns null") {
      val m   = mock[Result]
      val cfn = Bytes.toBytes("1")
      val cn  = Bytes.toBytes("2")

      when(m.getValue(cfn, cn)).thenReturn(null)

      val Right(v) = result.getValue[F](m, cfn, cn)

      assert(v.isEmpty)
      verify(m).getValue(cfn, cn)
    }
  }

  describe("getValueAsByteBuffer") {
    it("should return value with get from Result.getValueAsByteBuffer(Array[Byte], Array[Byte])") {
      val m   = mock[Result]
      val cfn = Bytes.toBytes("1")
      val cn  = Bytes.toBytes("2")

      val expected = "3"
      val re       = ByteBuffer.wrap(Bytes.toBytes(expected))

      when(m.getValueAsByteBuffer(cfn, cn)).thenReturn(re)

      val Right(Some(v)) = result.getValueAsByteBuffer[F](m, cfn, cn)

      assert(expected === Bytes.toString(v.array()))
      verify(m).getValueAsByteBuffer(cfn, cn)
    }
    it("should return empty when Result.getValueAsByteBuffer(Array[Byte], Array[Byte]) return null") {
      val m   = mock[Result]
      val cfn = Bytes.toBytes("1")
      val cn  = Bytes.toBytes("2")

      when(m.getValueAsByteBuffer(cfn, cn)).thenReturn(null)

      val Right(e) = result.getValueAsByteBuffer[F](m, cfn, cn)

      assert(e.isEmpty)
      verify(m).getValueAsByteBuffer(cfn, cn)
    }
  }
}
