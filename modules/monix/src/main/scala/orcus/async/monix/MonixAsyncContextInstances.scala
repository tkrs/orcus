package orcus.async.monix

import java.util.concurrent.{CompletableFuture, Executor}
import java.util.function.BiFunction

import monix.eval.Task
import monix.execution.Cancelable
import orcus.async.AsyncContext

trait MonixAsyncContextInstances {

  implicit val monixTaskAsyncContext: AsyncContext[Task] = new AsyncContext[Task] {
    def apply[A](f: CompletableFuture[A]): Task[A] = Task.async { (scheduler, cb) =>
      val _: CompletableFuture[Unit] = f.handleAsync(
        new BiFunction[A, Throwable, Unit] {
          def apply(t: A, u: Throwable): Unit = {
            if (u != null) cb.onError(u)
            else cb.onSuccess(t)
          }
        },
        new Executor {
          def execute(command: Runnable): Unit =
            scheduler.execute(command)
        }
      )
      Cancelable { () =>
        f.cancel(true); ()
      }
    }
  }
}
