package orcus.free
package iota

import _root_.iota.TListK.:::
import _root_.iota.{CopK, TNilK}
import cats.instances.try_._
import cats.~>
import orcus.free.handler.result
import org.scalatest.FunSuite

import scala.util.Try

class ResultOpsSpec extends FunSuite {

  type Algebra[A] = CopK[ResultOp ::: TNilK, A]

  def handler[F[_]: result.Handler]: Algebra ~> F = CopK.FunctionK.summon

  implicit val _handler: Algebra ~> Try = handler[Try]

  def ops[F[_] <: CopK[_, _]](implicit F: ResultOps[F]): ResultOps[F] = F

  test("ResultOps[Algebra]")(ops[Algebra])
}
