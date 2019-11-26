package orcus.free.handler

import cats.{~>, MonadError}
import orcus.free.ResultOp

object result {
  trait Handler[M[_]] extends (ResultOp ~> M)

  object Handler {
    import orcus.result._
    import ResultOp._

    implicit def resultOpHandler[M[_]](
      implicit
      ME: MonadError[M, Throwable]
    ): Handler[M] =
      new Handler[M] {

        override def apply[A](fa: ResultOp[A]): M[A] = fa match {
          case GetRow(result) =>
            getRow[M](result)
          case RawCells(result) =>
            rawCells[M](result)
          case GetColumnCells(result, family, qualifier) =>
            getColumnCells[M](result, family, qualifier)
          case GetColumnLatestCell(result, family, qualifier) =>
            getColumnLatestCell[M](result, family, qualifier)
          case g @ Get(_, _, _) =>
            g.run[M]
          case GetValue(result, family, qualifier) =>
            getValue[M](result, family, qualifier)
          case GetValueAsByteBuffer(result, family, qualifier) =>
            getValueAsByteBuffer(result, family, qualifier)
          case g @ GetFamily(_, _) =>
            g.run[M]
          case GetFamilyMap(result, family) =>
            getFamilyMap[M](result, family)
          case t @ To(_) =>
            t.run[M]
        }
      }
  }
}
