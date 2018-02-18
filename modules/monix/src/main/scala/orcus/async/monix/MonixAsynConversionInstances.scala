package orcus.async.monix

import java.util.concurrent.{CompletableFuture, Executor}
import java.util.function.BiConsumer

import monix.eval.Task
import monix.execution.Cancelable
import orcus.async.AsyncConversion

trait MonixAsynConversionInstances {

  implicit val monixTaskAsyncConversion: AsyncConversion[Task] = new AsyncConversion[Task] {
    def apply[A](f: CompletableFuture[A]): Task[A] = Task.async { (scheduler, cb) =>
      val _ = f.whenCompleteAsync(
        new BiConsumer[A, Throwable] {
          def accept(t: A, u: Throwable): Unit = {
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
