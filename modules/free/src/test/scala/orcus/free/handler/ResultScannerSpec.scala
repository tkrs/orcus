package orcus.free.handler

import java.nio.ByteBuffer

import cats.~>
import cats.instances.either._
import orcus.free.handler.resultScanner.Handler
import orcus.free.{ResultScannerOp, ResultScannerOps}
import org.apache.hadoop.hbase.client.{Result, ResultScanner}
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FunSpec, Matchers}

class ResultScannerSpec extends FunSpec with MockitoSugar with Matchers {

  type F[A] = Either[Throwable, A]

  def interpreter[M[_], A](implicit H: Handler[M]): ResultScannerOp ~> M = H

  def ops[M[_]](implicit T: ResultScannerOps[M]): ResultScannerOps[M] = T

  describe("Handler") {
    describe("nextOne") {
      it("should take the result successfully") {
        val m = mock[ResultScanner]
        val r = mock[Result]

        when(m.next()).thenReturn(r)

        val Right(Some(v)) = ops[ResultScannerOp]
          .nextOne(m)
          .foldMap(interpreter[F, Option[Result]])

        assert(v === r)
      }
    }
    describe("next") {
      it("should take the results successfully") {
        val m = mock[ResultScanner]
        val r = Iterator.continually(mock[Result]).take(10).toArray[Result]

        when(m.next(10)).thenReturn(r)

        val Right(v) = ops[ResultScannerOp]
          .next(m, 10)
          .foldMap(interpreter[F, ByteBuffer])

        assert(v === r.toSeq)
      }
    }
  }
}
