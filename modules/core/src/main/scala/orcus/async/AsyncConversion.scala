package orcus.async

import java.util.concurrent.CompletableFuture

import scala.concurrent.Future

trait AsyncConversion[F[_]] {
  def apply[A](f: CompletableFuture[A]): F[A]
}

object AsyncConversion {
  import scala.compat.java8.FutureConverters._

  def apply[F[_]](implicit F: AsyncConversion[F]): AsyncConversion[F] = F

  implicit lazy val scalaFutureAsyncContext: AsyncConversion[Future] =
    new AsyncConversion[Future] {
      def apply[A](f: CompletableFuture[A]): Future[A] = f.toScala
    }
}
