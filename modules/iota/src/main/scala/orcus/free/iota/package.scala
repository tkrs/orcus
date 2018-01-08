package orcus
package free

import _root_.iota.CopK

package iota {

  class ResultOps[M[_] <: CopK[_, _]](implicit inj: CopK.Inject[ResultOp, M]) extends ResultOps0[M]

  object ResultOps {
    implicit def resultOpsForIota[M[_] <: CopK[_, _]](
        implicit inj: CopK.Inject[ResultOp, M]): ResultOps[M] =
      new ResultOps
  }

  class ResultScannerOps[M[_] <: CopK[_, _]](implicit inj: CopK.Inject[ResultScannerOp, M])
      extends ResultScannerOps0[M]

  object ResultScannerOps {
    implicit def resultScannerOpsForIota[M[_] <: CopK[_, _]](
        implicit inj: CopK.Inject[ResultScannerOp, M]): ResultScannerOps[M] =
      new ResultScannerOps
  }

  class TableOps[M[_] <: CopK[_, _]](implicit inj: CopK.Inject[TableOp, M]) extends TableOps0[M]

  object TableOps {
    implicit def tableApiOpsForIota[M[_] <: CopK[_, _]](
        implicit inj: CopK.Inject[TableOp, M]): TableOps[M] =
      new TableOps
  }
}
