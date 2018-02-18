package orcus.async

import java.util.concurrent.CompletableFuture
import java.util.function.BiConsumer

import cats.~>

trait JCompletableFutureHandler {

  implicit def handleJavaCompletableFuture[F[_]](
      implicit F: AsyncHandler[F]): CompletableFuture ~> F = {

    def f[A](cf: CompletableFuture[A])(implicit F: AsyncHandler[F]): F[A] =
      F.handle[A](
        { cb =>
          val _ = cf.whenComplete(new BiConsumer[A, Throwable] {
            def accept(t: A, u: Throwable): Unit =
              if (u != null) cb(Left(u)) else cb(Right(t))
          })
          ()
        }, {
          val _ = cf.cancel(true)
        }
      )

    λ[CompletableFuture ~> F](f(_))
  }
}
