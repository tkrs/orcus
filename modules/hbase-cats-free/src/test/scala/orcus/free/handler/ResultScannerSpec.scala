package orcus.free.handler

import java.nio.ByteBuffer

import cats.~>
import orcus.free.ResultScannerOp
import orcus.free.ResultScannerOps
import orcus.free.handler.resultScanner.Handler
import org.apache.hadoop.hbase.client.Result
import org.apache.hadoop.hbase.client.ResultScanner
import org.mockito.Mockito._
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar

class ResultScannerSpec extends AnyFunSpec with MockitoSugar with Matchers {
  type F[A] = Either[Throwable, A]

  def interpreter[M[_], A](implicit H: Handler[M]): ResultScannerOp ~> M = H

  def ops[M[_]](implicit T: ResultScannerOps[M]): ResultScannerOps[M] = T

  describe("Handler") {
    describe("nextOne") {
      it("should take the result successfully") {
        val m = mock[ResultScanner]
        val r = mock[Result]

        when(m.next()).thenReturn(r)

        val v = ops[ResultScannerOp]
          .nextOne(m)
          .foldMap(interpreter[F, Option[Result]])

        assert(v === Right(Some(r)))
      }
    }
    describe("next") {
      it("should take the results successfully") {
        val m = mock[ResultScanner]
        val r = Iterator.continually(mock[Result]).take(10).toArray[Result]

        when(m.next(10)).thenReturn(r)

        val v = ops[ResultScannerOp]
          .next(m, 10)
          .foldMap(interpreter[F, ByteBuffer])

        assert(v === Right(r.toSeq))
      }
    }
  }
}
