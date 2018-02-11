package orcus.async.catseffect

import java.util.concurrent.{CompletableFuture, Executor}
import java.util.function.BiConsumer

import cats.effect.IO
import orcus.async.AsyncContext

import scala.concurrent.ExecutionContext

trait CatsEffectIOAsyncContextInstances {

  implicit def catsEffectIOAsyncContext(implicit ec: ExecutionContext): AsyncContext[IO] =
    new AsyncContext[IO] {
      def apply[A](f: CompletableFuture[A]): IO[A] = IO.async { cb =>
        val _ = f.whenCompleteAsync(
          new BiConsumer[A, Throwable] {
            def accept(t: A, u: Throwable): Unit = {
              if (u != null) cb(Left(u))
              else cb(Right(t))
            }
          },
          new Executor {
            def execute(command: Runnable): Unit =
              ec.execute(command)
          }
        )
        ()
      }
    }
}
