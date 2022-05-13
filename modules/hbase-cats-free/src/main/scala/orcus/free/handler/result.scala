package orcus.free.handler

import cats.MonadError
import cats.~>
import orcus.free.ResultOp

object result {
  trait Handler[M[_]] extends ResultOp ~> M

  object Handler {
    import orcus.result._
    import ResultOp._

    implicit def resultOpHandler[M[_]](implicit
      ME: MonadError[M, Throwable]
    ): Handler[M] =
      new Handler[M] {
        override def apply[A](fa: ResultOp[A]): M[A] =
          fa match {
            case GetRow(result) =>
              getRow[M](result)
            case RawCells(result) =>
              rawCells[M](result)
            case GetColumnCells(result, family, qualifier) =>
              getColumnCells[M](result, family, qualifier)
            case GetColumnLatestCell(result, family, qualifier) =>
              getColumnLatestCell[M](result, family, qualifier)
            case Get(result, family, qualifier, codec) =>
              implicit val _codec = codec
              get(result, family, qualifier)
            case GetValue(result, family, qualifier) =>
              getValue[M](result, family, qualifier)
            case GetValueAsByteBuffer(result, family, qualifier) =>
              getValueAsByteBuffer(result, family, qualifier)
            case GetFamily(result, family, codec) =>
              implicit val _codec = codec
              getFamily[A, M](result, family)
            case GetFamilyMap(result, family) =>
              getFamilyMap[M](result, family)
            case To(result, decoder) =>
              implicit val _decoder = decoder
              to(result)
          }
      }
  }
}
