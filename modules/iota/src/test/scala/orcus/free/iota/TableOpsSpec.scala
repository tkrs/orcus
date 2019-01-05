package orcus.free
package iota

import _root_.iota.TListK.:::
import _root_.iota.{CopK, TNilK}
import cats.data.Kleisli
import cats.instances.future._
import cats.~>
import orcus.{table => ot}
import orcus.async.future._
import orcus.free.handler.table
import org.scalatest.FunSuite

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class TableOpsSpec extends FunSuite {

  type Algebra[A] = CopK[TableOp ::: TNilK, A]

  def handler[F[_]: table.Handler]: Algebra ~> Kleisli[F, ot.AsyncTableT, ?] = CopK.FunctionK.summon

  implicit val _handler: Algebra ~> Kleisli[Future, ot.AsyncTableT, ?] =
    handler[Future]

  def ops[F[A] <: CopK[_, A]](implicit F: TableOps[F]): TableOps[F] = F

  test("TableOps[Algebra]")(ops[Algebra])
}
