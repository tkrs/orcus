package orcus.free
package iota

import _root_.iota.TListK.:::
import _root_.iota.{CopK, TNilK}
import cats.instances.try_._
import cats.~>
import orcus.free.handler.resultScanner
import org.scalatest.FunSuite

import scala.util.Try

class ResultScannerOpsSpec extends FunSuite {

  type Algebra[A] = CopK[ResultScannerOp ::: TNilK, A]

  def handler[F[_]: resultScanner.Handler]: Algebra ~> F = CopK.FunctionK.summon

  implicit val _handler: Algebra ~> Try = handler[Try]

  def ops[F[A] <: CopK[_, A]](implicit F: ResultScannerOps[F]): ResultScannerOps[F] = F

  test("ResultScannerOps[Algebra]")(ops[Algebra])
}
