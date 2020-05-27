package orcus.free

import cats.InjectK
import cats.free.Free
import org.apache.hadoop.hbase.client.{Result, ResultScanner}

trait ResultScannerApi[F[_]] {
  type ResultScannerF[A] = Free[F, A]

  def nextOne(resultScanner: ResultScanner): ResultScannerF[Option[Result]]
  def next(resultScanner: ResultScanner, i: Int): ResultScannerF[Seq[Result]]
}

sealed trait ResultScannerOp[A]

object ResultScannerOp {
  final case class NextOne(resultScanner: ResultScanner) extends ResultScannerOp[Option[Result]]
  final case class Next(resultScanner: ResultScanner, i: Int) extends ResultScannerOp[Seq[Result]]
}

abstract private[free] class ResultScannerOps0[M[_]](implicit inj: InjectK[ResultScannerOp, M])
    extends ResultScannerApi[M] {
  import ResultScannerOp._

  override def nextOne(resultScanner: ResultScanner): ResultScannerF[Option[Result]] =
    Free.inject[ResultScannerOp, M](NextOne(resultScanner))

  override def next(resultScanner: ResultScanner, i: Int): ResultScannerF[Seq[Result]] =
    Free.inject[ResultScannerOp, M](Next(resultScanner, i))
}

class ResultScannerOps[M[_]](implicit inj: InjectK[ResultScannerOp, M]) extends ResultScannerOps0[M]

object ResultScannerOps {
  implicit def resultScannerOps[M[_]](implicit inj: InjectK[ResultScannerOp, M]): ResultScannerOps[M] =
    new ResultScannerOps
}
