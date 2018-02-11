package orcus.async.twitterutil

import java.util.concurrent.{CompletableFuture, Executor}
import java.util.function.BiConsumer

import com.twitter.util.{Future, Promise}
import orcus.async.AsyncContext

trait TwitterUtilAsyncContextInstances {

  implicit def twitterUtilFutureAsyncContext(implicit e: Executor): AsyncContext[Future] =
    new AsyncContext[Future] {
      def apply[A](f: CompletableFuture[A]): Future[A] = {
        val p = Promise[A]
        val _ = f.whenCompleteAsync(
          new BiConsumer[A, Throwable] {
            def accept(t: A, u: Throwable): Unit =
              if (u != null) p.setException(u)
              else p.setValue(t)
          },
          e
        )
        p
      }
    }
}
