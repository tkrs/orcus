package orcus.async.instances

import orcus.async.AsyncHandler

import scala.concurrent.Future
import scala.concurrent.Promise

private[instances] trait AsyncHandlerScalaFutureInstances {
  implicit val handleScalaFuture: AsyncHandler[Future] =
    new AsyncHandler[Future] {
      def handle[A](callback: AsyncHandler.Callback[A], cancel: => Unit): Future[A] = {
        val p = Promise[A]()

        callback {
          case Right(v) =>
            val _ = p.success(v)
          case Left(e) =>
            val _ = p.failure(e)
        }

        p.future
      }
    }
}
