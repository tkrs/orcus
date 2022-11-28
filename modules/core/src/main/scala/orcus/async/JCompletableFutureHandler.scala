package orcus.async

import java.util.concurrent.CompletableFuture

import cats.~>

trait JCompletableFutureHandler {
  implicit def handleJavaCompletableFuture[F[_]](implicit F: AsyncHandler[F]): Par.Aux[CompletableFuture, F] =
    new Par[CompletableFuture] {
      type G[α] = F[α]

      def parallel: CompletableFuture ~> F = new (CompletableFuture ~> F) {
        def apply[A](cf: CompletableFuture[A]): F[A] =
          F.handle[A](
            cb => cf.whenComplete((t, u) => cb(if (u != null) Left(u) else Right(t))),
            cf.cancel(false)
          )
      }
    }
}
