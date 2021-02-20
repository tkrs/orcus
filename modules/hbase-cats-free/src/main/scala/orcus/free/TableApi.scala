package orcus.free

import cats.InjectK
import cats.free.Free
import orcus.BatchResult
import org.apache.hadoop.conf.{Configuration => HConfig}
import org.apache.hadoop.hbase.TableName
import org.apache.hadoop.hbase.client.Row
import org.apache.hadoop.hbase.client.{Append => HAppend}
import org.apache.hadoop.hbase.client.{Delete => HDelete}
import org.apache.hadoop.hbase.client.{Get => HGet}
import org.apache.hadoop.hbase.client.{Increment => HIncrement}
import org.apache.hadoop.hbase.client.{Put => HPut}
import org.apache.hadoop.hbase.client.{Result => HResult}
import org.apache.hadoop.hbase.client.{ResultScanner => HResultScanner}
import org.apache.hadoop.hbase.client.{Scan => HScan}

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
  def batch(actions: Seq[_ <: Row]): TableF[Seq[BatchResult]]
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
  final case class Batch(a: Seq[_ <: Row])  extends TableOp[Seq[BatchResult]]
}

abstract private[free] class TableOps0[M[_]](implicit inj: InjectK[TableOp, M]) extends TableApi[M] {
  import TableOp._

  override def getName: TableF[TableName] =
    Free.liftInject[M](GetName)

  override def getConfiguration: TableF[HConfig] =
    Free.liftInject[M](GetConfiguration)

  override def exists(a: HGet): TableF[Boolean] =
    Free.liftInject[M](Exists(a))

  override def get(a: HGet): TableF[HResult] =
    Free.liftInject[M](Get(a))

  override def put(a: HPut): TableF[Unit] =
    Free.liftInject[M](Put(a))

  override def getScanner(a: HScan): TableF[HResultScanner] =
    Free.liftInject[M](GetScanner(a))

  override def scanAll(a: HScan): TableF[Seq[HResult]] =
    Free.liftInject[M](ScanAll(a))

  override def delete(a: HDelete): TableF[Unit] =
    Free.liftInject[M](Delete(a))

  override def append(a: HAppend): TableF[HResult] =
    Free.liftInject[M](Append(a))

  override def increment(a: HIncrement): TableF[HResult] =
    Free.liftInject[M](Increment(a))

  override def batch(actions: Seq[_ <: Row]): TableF[Seq[BatchResult]] =
    Free.liftInject[M](Batch(actions))

  // override def existsAll(gets: Seq[Get]): TableF[Seq[Boolean]] = ???
}

class TableOps[M[_]](implicit inj: InjectK[TableOp, M]) extends TableOps0[M]

object TableOps {
  implicit def tableApiOps[M[_]](implicit inj: InjectK[TableOp, M]): TableOps[M] =
    new TableOps
}
