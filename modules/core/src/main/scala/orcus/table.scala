package orcus

import cats.MonadError
import cats.data.Kleisli
import org.apache.hadoop.conf.{Configuration => HConfig}
import org.apache.hadoop.hbase.client.{
  Append => HAppend,
  Delete => HDelete,
  Get => HGet,
  Increment => HIncrement,
  Put => HPut,
  Result => HResult,
  ResultScanner => HResultScanner,
  Scan => HScan,
  Table => HTable
}
import org.apache.hadoop.hbase.{HTableDescriptor, TableName => HTableName}

object table {

  def getName[F[_]](t: HTable)(
      implicit
      ME: MonadError[F, Throwable]
  ): F[HTableName] =
    ME.pure(t.getName)

  def getConfiguration[F[_]](t: HTable)(
      implicit
      ME: MonadError[F, Throwable]
  ): F[HConfig] =
    ME.pure(t.getConfiguration)

  def getTableDescriptor[F[_]](t: HTable)(
      implicit
      ME: MonadError[F, Throwable]
  ): F[HTableDescriptor] =
    ME.catchNonFatal(t.getTableDescriptor)

  def exists[F[_]](t: HTable, get: HGet)(
      implicit
      ME: MonadError[F, Throwable]
  ): F[Boolean] =
    ME.catchNonFatal(t.exists(get))

  def get[F[_]](t: HTable, a: HGet)(
      implicit
      ME: MonadError[F, Throwable]
  ): F[HResult] =
    ME.catchNonFatal(t.get(a))

  def put[F[_]](t: HTable, a: HPut)(
      implicit
      ME: MonadError[F, Throwable]
  ): F[Unit] =
    ME.catchNonFatal(t.put(a))

  def getScanner[F[_]](t: HTable, a: HScan)(
      implicit
      ME: MonadError[F, Throwable]
  ): F[HResultScanner] =
    ME.catchNonFatal(t.getScanner(a))

  def delete[F[_]](t: HTable, a: HDelete)(
      implicit ME: MonadError[F, Throwable]
  ): F[Unit] =
    ME.catchNonFatal(t.delete(a))

  def append[F[_]](t: HTable, a: HAppend)(
      implicit
      ME: MonadError[F, Throwable]
  ): F[HResult] =
    ME.catchNonFatal(t.append(a))

  def increment[F[_]](t: HTable, a: HIncrement)(
      implicit
      ME: MonadError[F, Throwable]
  ): F[HResult] =
    ME.catchNonFatal(t.increment(a))

  def close[F[_]](t: HTable)(
      implicit
      ME: MonadError[F, Throwable]
  ): F[Unit] =
    ME.catchNonFatal(t.close())

  def kleisli[F[_], A](f: HTable => F[A]): Kleisli[F, HTable, A] =
    Kleisli[F, HTable, A](f)
}
