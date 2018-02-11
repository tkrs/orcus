package orcus.async

import java.util.concurrent.CompletableFuture

import scala.concurrent.Future

trait AsyncContext[F[_]] {
  def apply[A](f: CompletableFuture[A]): F[A]
}

object AsyncContext {
  import scala.compat.java8.FutureConverters._

  def apply[F[_]](implicit F: AsyncContext[F]): AsyncContext[F] = F

  implicit lazy val scalaFutureAsyncContext: AsyncContext[Future] =
    new AsyncContext[Future] {
      def apply[A](f: CompletableFuture[A]): Future[A] = f.toScala
    }
}
