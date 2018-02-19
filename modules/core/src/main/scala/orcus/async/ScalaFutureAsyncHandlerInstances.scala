package orcus.async

import scala.concurrent.{Future, Promise}

trait ScalaFutureAsyncHandlerInstances {

  implicit lazy val handleScalaFuture: AsyncHandler[Future] =
    new AsyncHandler[Future] {
      def handle[A](callback: Callback[A], cancel: => Unit): Future[A] = {
        val p = Promise[A]

        callback {
          case Left(e) =>
            val _ = p.failure(e)
          case Right(v) =>
            val _ = p.success(v)
        }

        p.future
      }
    }
}
