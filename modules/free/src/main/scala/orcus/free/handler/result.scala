package orcus.free.handler

import cats.{MonadError, ~>}
import orcus.free.ResultOp

object result {

  trait Handler[M[_]] extends (ResultOp ~> M)

  object Handler {
    import orcus.result._
    import ResultOp._

    implicit def resultOpHandler[M[_]](
        implicit
        ME: MonadError[M, Throwable]): Handler[M] =
      new Handler[M] {
        override def apply[A](fa: ResultOp[A]): M[A] = fa match {
          case GetValue(result, family, qualifier) =>
            getValue[M](result, family, qualifier)
          case GetValueAsByteBuffer(result, family, qualifier) =>
            getValueAsByteBuffer(result, family, qualifier)
        }
      }
  }
}
