package orcus

import cats.{Monad, MonadError}
import cats.data.Kleisli
import orcus.async.AsyncContext
import org.apache.hadoop.conf.{Configuration => HConfig}
import org.apache.hadoop.hbase.client.{
  AsyncTable,
  ScanResultConsumerBase,
  Append => HAppend,
  Delete => HDelete,
  Get => HGet,
  Increment => HIncrement,
  Put => HPut,
  Result => HResult,
  ResultScanner => HResultScanner,
  Scan => HScan
}
import org.apache.hadoop.hbase.{TableName => HTableName}

object table {

  type AsyncTableT = AsyncTable[T] forSome { type T <: ScanResultConsumerBase }

  def getName[F[_]](t: AsyncTableT)(
      implicit
      ME: Monad[F]
  ): F[HTableName] =
    ME.pure(t.getName)

  def getConfiguration[F[_]](t: AsyncTableT)(
      implicit
      ME: Monad[F]
  ): F[HConfig] =
    ME.pure(t.getConfiguration)

  def exists[F[_]](t: AsyncTableT, get: HGet)(
      implicit
      ME: MonadError[F, Throwable],
      AC: AsyncContext[F]
  ): F[Boolean] =
    ME.map(AC(t.exists(get)))(_.booleanValue())

  def get[F[_]](t: AsyncTableT, a: HGet)(
      implicit
      AC: AsyncContext[F]
  ): F[HResult] =
    AC(t.get(a))

  def put[F[_]](t: AsyncTableT, a: HPut)(
      implicit
      ME: MonadError[F, Throwable],
      AC: AsyncContext[F]
  ): F[Unit] =
    ME.map(AC(t.put(a)))(_ => ())

  def getScanner[F[_]](t: AsyncTableT, a: HScan)(
      implicit
      ME: MonadError[F, Throwable]
  ): F[HResultScanner] =
    ME.catchNonFatal(t.getScanner(a))

  def delete[F[_]](t: AsyncTableT, a: HDelete)(
      implicit
      ME: MonadError[F, Throwable],
      AC: AsyncContext[F]
  ): F[Unit] =
    ME.map(AC(t.delete(a)))(_ => ())

  def append[F[_]](t: AsyncTableT, a: HAppend)(
      implicit
      AC: AsyncContext[F]
  ): F[HResult] =
    AC(t.append(a))

  def increment[F[_]](t: AsyncTableT, a: HIncrement)(
      implicit
      AC: AsyncContext[F]
  ): F[HResult] =
    AC(t.increment(a))

  def kleisli[F[_], A](f: AsyncTableT => F[A]): Kleisli[F, AsyncTableT, A] =
    Kleisli[F, AsyncTableT, A](f)
}
