package orcus

import cats.instances.either._
import org.apache.hadoop.hbase.client.{Result, ResultScanner}
import org.mockito.Mockito._
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar

class ResultScannerSpec extends AnyFunSpec with MockitoSugar with Matchers {
  type F[A] = Either[Throwable, A]

  describe("nextOne") {
    it("should take the result from ResultScanner.next() as-is") {
      val m = mock[ResultScanner]
      val expected = mock[Result]

      when(m.next()).thenReturn(expected)

      val v = resultScanner.nextOne[F](m)

      assert(v === Right(Some(expected)))
      verify(m).next()
    }
    it("should return empty when ResultScanner.next() returns null") {
      val m = mock[ResultScanner]

      when(m.next()).thenReturn(null)

      val v = resultScanner.nextOne[F](m)

      assert(v === Right(None))
      verify(m).next()
    }
  }

  describe("next") {
    it("should take the results from ResultScanner.next(Int) and convert it to scala's Seq") {
      val m = mock[ResultScanner]
      val expected = Iterator.continually(mock[Result]).take(10).toArray[Result]

      when(m.next(10)).thenReturn(expected)

      val vs = resultScanner.next[F](m, 10)

      assert(vs === Right(expected.toSeq))
      verify(m).next(10)
    }
  }
}
