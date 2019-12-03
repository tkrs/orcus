package orcus.async

import java.util.concurrent.CompletableFuture
import java.util.function.BiConsumer

import cats.~>

trait JCompletableFutureHandler {
  implicit def handleJavaCompletableFuture[F[_]](implicit F: AsyncHandler[F]): Par.Aux[CompletableFuture, F] =
    new Par[CompletableFuture] {
      type G[α] = F[α]

      def parallel: CompletableFuture ~> F = λ[CompletableFuture ~> F](toF(_))

      private def toF[A](cf: CompletableFuture[A]): F[A] =
        F.handle[A](
          { cb =>
            val f = new BiConsumer[A, Throwable] {
              def accept(t: A, u: Throwable): Unit =
                if (u != null) cb(Left(u)) else cb(Right(t))
            }
            val _ = cf.whenComplete(f)
          }, {
            val _ = cf.cancel(false)
          }
        )
    }
}
