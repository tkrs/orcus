package orcus

import java.util.concurrent.CompletableFuture

import cats.instances.vector._
import cats.syntax.traverse._
import cats.{Applicative, Monad, MonadError, ~>}
import cats.data.Kleisli
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
  Row => HRow,
  Scan => HScan
}
import org.apache.hadoop.hbase.{TableName => HTableName}

import scala.collection.JavaConverters._

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
      F: CompletableFuture ~> F
  ): F[Boolean] =
    ME.map(F(t.exists(get)))(_.booleanValue())

  def get[F[_]](t: AsyncTableT, a: HGet)(
      implicit
      F: CompletableFuture ~> F
  ): F[HResult] =
    F(t.get(a))

  def put[F[_]](t: AsyncTableT, a: HPut)(
      implicit
      ME: MonadError[F, Throwable],
      F: CompletableFuture ~> F
  ): F[Unit] =
    ME.map(F(t.put(a)))(_ => ())

  def getScanner[F[_]](t: AsyncTableT, a: HScan)(
      implicit
      ME: MonadError[F, Throwable]
  ): F[HResultScanner] =
    ME.catchNonFatal(t.getScanner(a))

  def delete[F[_]](t: AsyncTableT, a: HDelete)(
      implicit
      ME: MonadError[F, Throwable],
      F: CompletableFuture ~> F
  ): F[Unit] =
    ME.map(F(t.delete(a)))(_ => ())

  def append[F[_]](t: AsyncTableT, a: HAppend)(
      implicit
      F: CompletableFuture ~> F
  ): F[HResult] =
    F(t.append(a))

  def increment[F[_]](t: AsyncTableT, a: HIncrement)(
      implicit
      F: CompletableFuture ~> F
  ): F[HResult] =
    F(t.increment(a))

  def batch[T <: HRow](t: AsyncTableT, as: Seq[T]): Iterator[CompletableFuture[T]] =
    t.batch[T](as.asJava).iterator().asScala

  def batchS[F[_]: Applicative, T <: HRow](t: AsyncTableT, as: Seq[T])(
      implicit
      ME: MonadError[F, Throwable],
      F: CompletableFuture ~> F
  ): F[Vector[Option[T]]] =
    batch[T](t, as).map(a => ME.map(F(a))(Option.apply)).toVector.sequence[F, Option[T]]

  def batchT[F[_], T <: HRow](t: AsyncTableT, as: Seq[T])(
      implicit
      ME: MonadError[F, Throwable],
      F: CompletableFuture ~> F
  ): F[Vector[Option[T]]] =
    batch[T](t, as).toVector.traverse(a => ME.map(F(a))(Option.apply))

  def batchAll[F[_], T <: HRow](t: AsyncTableT, as: Seq[T])(
      implicit
      ME: MonadError[F, Throwable],
      F: CompletableFuture ~> F
  ): F[Vector[Option[T]]] =
    ME.map(F(t.batchAll[T](as.asJava)))(_.iterator().asScala.map(Option.apply).toVector)

  def kleisli[F[_], A](f: AsyncTableT => F[A]): Kleisli[F, AsyncTableT, A] =
    Kleisli[F, AsyncTableT, A](f)
}
