package orcus.async

import scala.concurrent.{ExecutionContext, Future, Promise}

trait ScalaFutureAsyncHandlerInstances {

  implicit def handleScalaFuture(implicit ec: ExecutionContext): AsyncHandler[Future] =
    new AsyncHandler[Future] {
      def handle[A](callback: Callback[A], cancel: => Unit): Future[A] = {
        val p = Promise[A]

        ec.execute(new Runnable {
          def run(): Unit = callback {
            case Left(e) =>
              val _ = p.failure(e)
            case Right(v) =>
              val _ = p.success(v)
          }
        })

        p.future
      }
    }
}
