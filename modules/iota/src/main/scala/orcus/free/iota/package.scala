package orcus
package free

import _root_.iota.CopK

package iota {

  class ResultOps[M[α] <: CopK[_, α]](implicit inj: CopK.Inject[ResultOp, M]) extends ResultOps0[M]

  object ResultOps {
    implicit def resultOpsForIota[M[α] <: CopK[_, α]](implicit inj: CopK.Inject[ResultOp, M]): ResultOps[M] =
      new ResultOps
  }

  class ResultScannerOps[M[α] <: CopK[_, α]](implicit inj: CopK.Inject[ResultScannerOp, M]) extends ResultScannerOps0[M]

  object ResultScannerOps {
    implicit def resultScannerOpsForIota[M[α] <: CopK[_, α]](
        implicit inj: CopK.Inject[ResultScannerOp, M]): ResultScannerOps[M] =
      new ResultScannerOps
  }

  class TableOps[M[α] <: CopK[_, α]](implicit inj: CopK.Inject[TableOp, M]) extends TableOps0[M]

  object TableOps {
    implicit def tableApiOpsForIota[M[α] <: CopK[_, α]](implicit inj: CopK.Inject[TableOp, M]): TableOps[M] =
      new TableOps
  }
}
