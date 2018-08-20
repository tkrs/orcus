package orcus.free

import cats.{ApplicativeError, InjectK}
import cats.free.Free
import orcus.BatchResult
import orcus.async.Par
import orcus.table.AsyncTableT
import org.apache.hadoop.conf.{Configuration => HConfig}
import org.apache.hadoop.hbase.TableName
import org.apache.hadoop.hbase.client.{
  Row,
  Append => HAppend,
  Delete => HDelete,
  Get => HGet,
  Increment => HIncrement,
  Put => HPut,
  Result => HResult,
  ResultScanner => HResultScanner,
  Scan => HScan
}

import scala.collection.generic.CanBuildFrom

trait TableApi[F[_]] {
  type TableF[A] = Free[F, A]

  def getName: TableF[TableName]
  def getConfiguration: TableF[HConfig]
  def exists(get: HGet): TableF[Boolean]
  def get(a: HGet): TableF[HResult]
  def put(a: HPut): TableF[Unit]
  def getScanner(a: HScan): TableF[HResultScanner]
  def scanAll(a: HScan): TableF[Seq[HResult]]
  def delete(a: HDelete): TableF[Unit]
  def append(a: HAppend): TableF[HResult]
  def increment(a: HIncrement): TableF[HResult]
  def batch[C[_]](actions: Seq[_ <: Row]): TableF[C[BatchResult]]
  // def existsAll(gets: Seq[Get]): TableF[Seq[Boolean]]
}

sealed trait TableOp[A]

object TableOp {
  final case object GetName                 extends TableOp[TableName]
  final case object GetConfiguration        extends TableOp[HConfig]
  final case class Exists(a: HGet)          extends TableOp[Boolean]
  final case class Get(a: HGet)             extends TableOp[HResult]
  final case class Put(a: HPut)             extends TableOp[Unit]
  final case class GetScanner(a: HScan)     extends TableOp[HResultScanner]
  final case class ScanAll(a: HScan)        extends TableOp[Seq[HResult]]
  final case class Delete(a: HDelete)       extends TableOp[Unit]
  final case class Append(a: HAppend)       extends TableOp[HResult]
  final case class Increment(a: HIncrement) extends TableOp[HResult]
  final case class Batch[C[_]](a: Seq[_ <: Row]) extends TableOp[C[BatchResult]] {
    def run[M[_]](t: AsyncTableT)(implicit
                                  ME: ApplicativeError[M, Throwable],
                                  cf: Par[M],
                                  cbf: CanBuildFrom[Nothing, BatchResult, C[BatchResult]]): M[C[BatchResult]] =
      orcus.table.batch[M, C](t, a)
  }
}

private[free] abstract class TableOps0[M[_]](implicit inj: InjectK[TableOp, M]) extends TableApi[M] {
  import TableOp._

  override def getName: TableF[TableName] =
    Free.inject[TableOp, M](GetName)

  override def getConfiguration: TableF[HConfig] =
    Free.inject[TableOp, M](GetConfiguration)

  override def exists(a: HGet): TableF[Boolean] =
    Free.inject[TableOp, M](Exists(a))

  override def get(a: HGet): TableF[HResult] =
    Free.inject[TableOp, M](Get(a))

  override def put(a: HPut): TableF[Unit] =
    Free.inject[TableOp, M](Put(a))

  override def getScanner(a: HScan): TableF[HResultScanner] =
    Free.inject[TableOp, M](GetScanner(a))

  override def scanAll(a: HScan): TableF[Seq[HResult]] =
    Free.inject[TableOp, M](ScanAll(a))

  override def delete(a: HDelete): TableF[Unit] =
    Free.inject[TableOp, M](Delete(a))

  override def append(a: HAppend): TableF[HResult] =
    Free.inject[TableOp, M](Append(a))

  override def increment(a: HIncrement): TableF[HResult] =
    Free.inject[TableOp, M](Increment(a))

  override def batch[C[_]](actions: Seq[_ <: Row]): TableF[C[BatchResult]] =
    Free.inject[TableOp, M](Batch[C](actions))

  // override def existsAll(gets: Seq[Get]): TableF[Seq[Boolean]] = ???
}

class TableOps[M[_]](implicit inj: InjectK[TableOp, M]) extends TableOps0[M]

object TableOps {
  implicit def tableApiOps[M[_]](implicit inj: InjectK[TableOp, M]): TableOps[M] =
    new TableOps
}
