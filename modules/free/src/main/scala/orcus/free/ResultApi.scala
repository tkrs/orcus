package orcus.free

import java.nio.ByteBuffer

import cats.free.{Free, Inject}
import org.apache.hadoop.hbase.Cell
import org.apache.hadoop.hbase.client.Result

trait ResultApi[F[_]] {
  type ResultF[A] = Free[F, A]

  def getRow(r: Result): ResultF[Option[Array[Byte]]]

  def rawCells(r: Result): ResultF[Seq[Cell]]

  def getColumnCells(r: Result, family: Array[Byte], qualifier: Array[Byte]): ResultF[Seq[Cell]]

  def getColumnLatestCell(r: Result,
                          family: Array[Byte],
                          qualifier: Array[Byte]): ResultF[Option[Cell]]

  def getValue(result: Result,
               family: Array[Byte],
               qualifier: Array[Byte]): ResultF[Option[Array[Byte]]]

  def getValueAsByteBuffer(result: Result,
                           family: Array[Byte],
                           qualifier: Array[Byte]): ResultF[Option[ByteBuffer]]

  def getFamilyMap(result: Result, family: Array[Byte]): ResultF[Map[Array[Byte], Array[Byte]]]
}

sealed trait ResultOp[A]

object ResultOp {
  final case class GetRow(result: Result)   extends ResultOp[Option[Array[Byte]]]
  final case class RawCells(result: Result) extends ResultOp[Seq[Cell]]
  final case class GetColumnCells(result: Result, family: Array[Byte], qualifier: Array[Byte])
      extends ResultOp[Seq[Cell]]
  final case class GetColumnLatestCell(result: Result, family: Array[Byte], qualifier: Array[Byte])
      extends ResultOp[Option[Cell]]
  final case class GetValue(result: Result, family: Array[Byte], qualifier: Array[Byte])
      extends ResultOp[Option[Array[Byte]]]
  final case class GetValueAsByteBuffer(result: Result, family: Array[Byte], qualifier: Array[Byte])
      extends ResultOp[Option[ByteBuffer]]
  final case class GetFamilyMap(result: Result, family: Array[Byte])
      extends ResultOp[Map[Array[Byte], Array[Byte]]]
}

class ResultOps[M[_]](implicit inj: Inject[ResultOp, M]) extends ResultApi[M] {
  import ResultOp._

  override def getRow(r: Result): ResultF[Option[Array[Byte]]] =
    Free.inject[ResultOp, M](GetRow(r))

  override def rawCells(r: Result): ResultF[Seq[Cell]] =
    Free.inject[ResultOp, M](RawCells(r))

  override def getColumnCells(r: Result,
                              family: Array[Byte],
                              qualifier: Array[Byte]): ResultF[Seq[Cell]] =
    Free.inject[ResultOp, M](GetColumnCells(r, family, qualifier))

  override def getColumnLatestCell(r: Result,
                                   family: Array[Byte],
                                   qualifier: Array[Byte]): ResultF[Option[Cell]] =
    Free.inject[ResultOp, M](GetColumnLatestCell(r, family, qualifier))

  override def getValue(result: Result,
                        family: Array[Byte],
                        qualifier: Array[Byte]): ResultF[Option[Array[Byte]]] =
    Free.inject[ResultOp, M](GetValue(result, family, qualifier))

  override def getValueAsByteBuffer(result: Result,
                                    family: Array[Byte],
                                    qualifier: Array[Byte]): ResultF[Option[ByteBuffer]] =
    Free.inject[ResultOp, M](GetValueAsByteBuffer(result, family, qualifier))

  override def getFamilyMap(result: Result,
                            family: Array[Byte]): ResultF[Map[Array[Byte], Array[Byte]]] =
    Free.inject[ResultOp, M](GetFamilyMap(result, family))
}

object ResultOps {
  implicit def resultOps[M[_]](implicit inj: Inject[ResultOp, M]): ResultOps[M] = new ResultOps
}
