package orcus

import cats.instances.either._
import org.apache.hadoop.hbase.client.{Result, ResultScanner}
import org.scalatest.{FunSpec, Matchers}
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito._

class ResultScannerSpec extends FunSpec with MockitoSugar with Matchers {

  type F[A] = Either[Throwable, A]

  describe("nextOne") {
    it("should return Result with get from ResultScanner.next()") {
      val m        = mock[ResultScanner]
      val expected = mock[Result]

      when(m.next()).thenReturn(expected)

      val Right(Some(v)) = resultScanner.nextOne[F](m)

      assert(expected === v)
      verify(m).next()
    }
    it("should return None when ResultScanner.next() returns null") {
      val m = mock[ResultScanner]

      when(m.next()).thenReturn(null)

      val Right(v) = resultScanner.nextOne[F](m)

      assert(v.isEmpty)
      verify(m).next()
    }
  }

  describe("next") {
    it("should return Result with get from ResultScanner.next(Int)") {
      val m        = mock[ResultScanner]
      val expected = Iterator.continually(mock[Result]).take(10).toArray[Result]

      when(m.next(10)).thenReturn(expected)

      val Right(vs) = resultScanner.next[F](m, 10)

      assert(expected.toSeq === vs)
      verify(m).next(10)
    }
  }
}
