package orcus.free

import cats.free.{Free, Inject}
import org.apache.hadoop.conf.{Configuration => HConfig}
import org.apache.hadoop.hbase.{HTableDescriptor, TableName}
import org.apache.hadoop.hbase.client.{
  Append => HAppend,
  Delete => HDelete,
  Get => HGet,
  Increment => HIncrement,
  Put => HPut,
  Result => HResult,
  ResultScanner => HResultScanner,
  Scan => HScan
}

trait TableApi[F[_]] {
  type TableF[A] = Free[F, A]

  def name: TableF[TableName]
  def configuration: TableF[HConfig]
  def descriptor: TableF[HTableDescriptor]
  def exists(get: HGet): TableF[Boolean]
  // def existsAll(gets: Seq[Get]): TableF[Seq[Boolean]]
  // def batch[A](actions: Seq[Row]): TableF[Seq[A]]
  def get(a: HGet): TableF[HResult]
  def put(a: HPut): TableF[Unit]
  def scan(a: HScan): TableF[HResultScanner]
  def delete(a: HDelete): TableF[Unit]
  def append(a: HAppend): TableF[HResult]
  def increment(a: HIncrement): TableF[HResult]
  def close(): TableF[Unit]
}

sealed trait TableOp[A]

object TableOp {
  final case object Name                    extends TableOp[TableName]
  final case object Configuration           extends TableOp[HConfig]
  final case object Descriptor              extends TableOp[HTableDescriptor]
  final case class Exists(a: HGet)          extends TableOp[Boolean]
  final case class Get(a: HGet)             extends TableOp[HResult]
  final case class Put(a: HPut)             extends TableOp[Unit]
  final case class Scan(a: HScan)           extends TableOp[HResultScanner]
  final case class Delete(a: HDelete)       extends TableOp[Unit]
  final case class Append(a: HAppend)       extends TableOp[HResult]
  final case class Increment(a: HIncrement) extends TableOp[HResult]
  final case object Close                   extends TableOp[Unit]
}

final class TableOps[M[_]](implicit inj: Inject[TableOp, M]) extends TableApi[M] {
  import TableOp._

  override def name: TableF[TableName] =
    Free.inject[TableOp, M](Name)

  override def configuration: TableF[HConfig] =
    Free.inject[TableOp, M](Configuration)

  override def descriptor: TableF[HTableDescriptor] =
    Free.inject[TableOp, M](Descriptor)

  override def exists(a: HGet): TableF[Boolean] =
    Free.inject[TableOp, M](Exists(a))

  // override def existsAll(gets: Seq[Get]): TableF[Seq[Boolean]] = ???

  // override def batch[A](actions: Seq[Row]): TableF[Seq[A]] = ???

  override def get(a: HGet): TableF[HResult] =
    Free.inject[TableOp, M](Get(a))

  override def put(a: HPut): TableF[Unit] =
    Free.inject[TableOp, M](Put(a))

  override def scan(a: HScan): TableF[HResultScanner] =
    Free.inject[TableOp, M](Scan(a))

  override def delete(a: HDelete): TableF[Unit] =
    Free.inject[TableOp, M](Delete(a))

  override def append(a: HAppend): TableF[HResult] =
    Free.inject[TableOp, M](Append(a))

  override def increment(a: HIncrement): TableF[HResult] =
    Free.inject[TableOp, M](Increment(a))

  override def close(): TableF[Unit] =
    Free.inject[TableOp, M](Close)
}

object TableOps {
  implicit def tableApiOps[M[_]](implicit inj: Inject[TableOp, M]): TableOps[M] =
    new TableOps
}
