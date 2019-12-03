package orcus.async.future

import orcus.async.{AsyncHandler, Callback}

import scala.concurrent.{Future, Promise}

private[future] trait AsyncHandlerInstances {
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
