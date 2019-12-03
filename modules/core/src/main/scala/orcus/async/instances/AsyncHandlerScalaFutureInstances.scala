package orcus.async.instances

import orcus.async.AsyncHandler

import scala.concurrent.{Future, Promise}

private[instances] trait AsyncHandlerScalaFutureInstances {
  implicit val handleScalaFuture: AsyncHandler[Future] =
    new AsyncHandler[Future] {
      def handle[A](callback: AsyncHandler.Callback[A], cancel: => Unit): Future[A] = {
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
