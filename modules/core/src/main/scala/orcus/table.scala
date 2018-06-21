package orcus

import java.util.concurrent.CompletableFuture

import cats.{Monad, MonadError, ~>}
import cats.data.Kleisli
import org.apache.hadoop.conf.{Configuration => HConfig}
import org.apache.hadoop.hbase.client.{
  AsyncTable,
  RowMutations,
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
import scala.collection.generic.CanBuildFrom

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

  def scanAll[F[_]](t: AsyncTableT, a: HScan)(
      implicit
      ME: MonadError[F, Throwable],
      F: CompletableFuture ~> F
  ): F[Seq[HResult]] =
    ME.map(F(t.scanAll(a)))(_.asScala)

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

  def batch[F[_], C[_]](t: AsyncTableT, as: Seq[_ <: HRow])(
      implicit
      ME: MonadError[F, Throwable],
      F: CompletableFuture ~> F,
      cbf: CanBuildFrom[Nothing, BatchResult, C[BatchResult]]
  ): F[C[BatchResult]] = {
    val itr   = as.iterator
    val itcfo = t.batch[Object](as.asJava).iterator.asScala
    val itfb = itr
      .zip(itcfo.map(F.apply))
      .map {
        case (a, fo) =>
          val f = ME.flatMap[Object, BatchResult](fo) {
            case r: HResult =>
              ME.pure(BatchResult.Mutate(Some(r)))
            case null =>
              a match {
                case _: HGet | _: HAppend | _: HIncrement | _: RowMutations =>
                  ME.pure(BatchResult.Mutate(None))
                case _ => // Delete or Put
                  ME.pure(BatchResult.VoidMutate)
              }
            case other =>
              ME.pure(
                BatchResult.Error(new Exception(s"Unexpected class returned: ${other.getClass.getSimpleName}"), a))
          }
          ME.recover(f) {
            case t: Throwable =>
              BatchResult.Error(t, a)
          }
      }
    val fbb = itfb.foldLeft(ME.pure(cbf.apply)) {
      case (acc, fb) => ME.map2(fb, acc)((a, b) => b += a)
    }
    ME.map(fbb)(_.result)
  }

  def batchAll[F[_], C[_]](t: AsyncTableT, as: Seq[_ <: HRow])(
      implicit
      ME: MonadError[F, Throwable],
      F: CompletableFuture ~> F,
      C: CanBuildFrom[Nothing, Option[HResult], C[Option[HResult]]]
  ): F[C[Option[HResult]]] = {
    ME.map(F(t.batchAll[Object](as.asJava))) { xs =>
      val it = xs.iterator
      val c  = C.apply
      while (it.hasNext) c += (it.next match { case r: HResult => Option(r); case null => None })
      c.result
    }
  }

  def kleisli[F[_], A](f: AsyncTableT => F[A]): Kleisli[F, AsyncTableT, A] =
    Kleisli[F, AsyncTableT, A](f)
}
