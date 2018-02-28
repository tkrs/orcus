package orcus.free
package iota

import _root_.iota.TListK.:::
import _root_.iota.{CopK, TNilK}
import cats.data.Kleisli
import cats.instances.try_._
import cats.~>
import orcus.free.handler.table
import org.apache.hadoop.hbase.client.Table
import org.scalatest.FunSuite

import scala.util.Try

class TableOpsSpec extends FunSuite {

  type Algebra[A] = CopK[TableOp ::: TNilK, A]

  def handler[F[_]: table.Handler]: Algebra ~> Kleisli[F, Table, ?] = CopK.FunctionK.summon

  implicit val _handler: Algebra ~> Kleisli[Try, Table, ?] = handler[Try]

  def ops[F[A] <: CopK[_, A]](implicit F: TableOps[F]): TableOps[F] = F

  test("TableOps[Algebra]")(ops[Algebra])
}
