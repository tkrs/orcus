package orcus.bigtable

import cats.data.Kleisli
import cats.{Monad, MonadError}
import com.google.api.core.ApiFuture
import com.google.api.gax.rpc.{ResponseObserver, StreamController}
import com.google.cloud.bigtable.data.v2.BigtableDataClient
import com.google.cloud.bigtable.data.v2.models._
import orcus.async.Par
import orcus.bigtable.codec.RowDecoder
import orcus.internal.Utils

import scala.annotation.tailrec
import scala.collection.mutable

class BigtableDataClientWrapper[F[_]](client: BigtableDataClient)(
  implicit
  F: MonadError[F, Throwable],
  parF: Par.Aux[ApiFuture, F]
) {
  private[this] val adapter = BigtableDataClientAdapter

  def readRowAsync[A: RowDecoder](query: Query): F[Option[A]] =
    adapter.readRowAsync(client, query)

  def readRowsAsync[A: RowDecoder](cb: Either[Throwable, List[A]] => Unit, query: Query): Unit =
    adapter.readRowsAsync(client, cb, query)

  def sampleRowKeysAsync(tableId: String): F[List[KeyOffset]] =
    adapter.sampleRowKeysAsync(client, tableId)

  def mutateRowAsync(mutation: RowMutation): F[Unit] =
    adapter.mutateRowAsync(client, mutation)

  def bulkMutateRowsAsync(mutation: BulkMutation): F[Unit] =
    adapter.bulkMutateRowsAsync(client, mutation)

  def checkAndMutateRowAsync(mutation: ConditionalRowMutation): F[Boolean] =
    adapter.checkAndMutateRowAsync(client, mutation)

  def readModifyWriteRowAsync[A: RowDecoder](mutation: ReadModifyWriteRow): F[Option[A]] =
    adapter.readModifyWriteRowAsync(client, mutation)

  def close(): Unit =
    adapter.close(client)
}

class BigtableDataClientWrapperK[F[_]]()(
  implicit
  F: MonadError[F, Throwable],
  parF: Par.Aux[ApiFuture, F]
) {
  private[this] val adapter = BigtableDataClientAdapter

  def readRowAsync[A: RowDecoder](query: Query): Kleisli[F, BigtableDataClient, Option[A]] =
    Kleisli(adapter.readRowAsync(_, query))

  def readRowsAsync[A: RowDecoder](
    cb: Either[Throwable, List[A]] => Unit,
    query: Query
  ): Kleisli[F, BigtableDataClient, Unit] =
    Kleisli(c => F.pure(adapter.readRowsAsync(c, cb, query)))

  def sampleRowKeysAsync(tableId: String): Kleisli[F, BigtableDataClient, List[KeyOffset]] =
    Kleisli(adapter.sampleRowKeysAsync(_, tableId))

  def mutateRowAsync(mutation: RowMutation): Kleisli[F, BigtableDataClient, Unit] =
    Kleisli(adapter.mutateRowAsync(_, mutation))

  def bulkMutateRowsAsync(mutation: BulkMutation): Kleisli[F, BigtableDataClient, Unit] =
    Kleisli(adapter.bulkMutateRowsAsync(_, mutation))

  def checkAndMutateRowAsync(mutation: ConditionalRowMutation): Kleisli[F, BigtableDataClient, Boolean] =
    Kleisli(adapter.checkAndMutateRowAsync(_, mutation))

  def readModifyWriteRowAsync[A: RowDecoder](mutation: ReadModifyWriteRow): Kleisli[F, BigtableDataClient, Option[A]] =
    Kleisli(adapter.readModifyWriteRowAsync(_, mutation))

  def close(): Kleisli[F, BigtableDataClient, Unit] =
    Kleisli(c => F.pure(adapter.close(c)))
}

object BigtableDataClientAdapter {
  import cats.implicits._

  def readRowAsync[F[_], A: RowDecoder](
    client: BigtableDataClient,
    query: Query
  )(implicit F: MonadError[F, Throwable], parF: Par.Aux[ApiFuture, F]): F[Option[A]] =
    F.flatMap(parF.parallel(client.readRowCallable.futureCall(query))) {
      case null => F.pure(none)
      case row  => F.fromEither(RowDecoder[A].apply(decode(row))).map(Option.apply)
    }

  def readRowsAsync[A: RowDecoder](
    client: BigtableDataClient,
    cb: Either[Throwable, List[A]] => Unit,
    query: Query
  ): Unit =
    client.readRowsAsync(
      query,
      new ResponseObserver[Row] {
        private[this] var controller: StreamController = _
        private[this] val acc                          = List.newBuilder[A]

        def onStart(controller: StreamController): Unit =
          this.controller = controller

        def onResponse(response: Row): Unit =
          RowDecoder[A].apply(decode(response)) match {
            case Right(v) =>
              acc += v
            case Left(e) =>
              controller.cancel()
              cb(e.asLeft)
          }

        def onError(e: Throwable): Unit =
          cb(e.asLeft)

        def onComplete(): Unit = {
          val xs = acc.result()
          cb(xs.asRight)
        }
      }
    )

  def sampleRowKeysAsync[F[_]](
    client: BigtableDataClient,
    tableId: String
  )(implicit F: Monad[F], parF: Par.Aux[ApiFuture, F]): F[List[KeyOffset]] =
    parF.parallel(client.sampleRowKeysAsync(tableId)).map(Utils.toList)

  def mutateRowAsync[F[_]](client: BigtableDataClient, rowMutation: RowMutation)(
    implicit
    F: Monad[F],
    parF: Par.Aux[ApiFuture, F]
  ): F[Unit] =
    parF.parallel(client.mutateRowAsync(rowMutation)) >> F.unit

  def bulkMutateRowsAsync[F[_]](client: BigtableDataClient, mutation: BulkMutation)(
    implicit
    F: Monad[F],
    parF: Par.Aux[ApiFuture, F]
  ): F[Unit] =
    parF.parallel(client.bulkMutateRowsAsync(mutation)) >> F.unit

  def checkAndMutateRowAsync[F[_]](client: BigtableDataClient, mutation: ConditionalRowMutation)(
    implicit
    F: Monad[F],
    parF: Par.Aux[ApiFuture, F]
  ): F[Boolean] =
    parF.parallel(client.checkAndMutateRowAsync(mutation)).map(Boolean.unbox)

  def readModifyWriteRowAsync[F[_], A: RowDecoder](client: BigtableDataClient, mutation: ReadModifyWriteRow)(
    implicit
    F: MonadError[F, Throwable],
    parF: Par.Aux[ApiFuture, F]
  ): F[Option[A]] =
    F.flatMap(parF.parallel(client.readModifyWriteRowAsync(mutation)).map(Option.apply)) {
      case Some(row) => F.fromEither(RowDecoder[A].apply(decode(row))).map(Option.apply)
      case _         => F.pure(none)
    }

  def close(client: BigtableDataClient): Unit = client.close()

  private def decode(row: Row): CRow = {
    val acc   = Map.newBuilder[String, List[RowCell]]
    val cells = row.getCells
    val size  = cells.size()

    @tailrec def loop2(
      currentFamily: String,
      i: Int,
      b: mutable.Builder[RowCell, List[RowCell]]
    ): (Int, List[RowCell]) =
      if (i >= size) i -> b.result()
      else {
        val cell = cells.get(i)
        if (cell.getFamily != currentFamily) i -> b.result()
        else loop2(currentFamily, i + 1, b += cell)
      }

    @tailrec def loop(i: Int): Map[String, List[RowCell]] =
      if (i >= size) acc.result()
      else {
        val family   = cells.get(i).getFamily
        val (ii, xs) = loop2(family, i, List.newBuilder)
        acc += family -> xs
        loop(ii)
      }

    CRow(row.getKey.toStringUtf8, loop(0))
  }
}
