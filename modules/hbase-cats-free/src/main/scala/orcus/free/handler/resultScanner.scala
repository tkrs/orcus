package orcus.free.handler

import cats.MonadError
import cats.~>
import orcus.free.ResultScannerOp

object resultScanner {
  trait Handler[M[_]] extends ResultScannerOp ~> M

  object Handler {
    import orcus.resultScanner._
    import ResultScannerOp._

    implicit def resultScannerOpHandler[M[_]](implicit
      ME: MonadError[M, Throwable]
    ): Handler[M] =
      new Handler[M] {
        override def apply[A](fa: ResultScannerOp[A]): M[A] =
          fa match {
            case NextOne(rs) => nextOne[M](rs)
            case Next(rs, i) => next[M](rs, i)
          }
      }
  }
}
