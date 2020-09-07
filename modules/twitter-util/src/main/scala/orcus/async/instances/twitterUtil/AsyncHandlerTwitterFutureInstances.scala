package orcus.async.instances.twitterUtil

import com.twitter.util.Future
import com.twitter.util.Promise
import orcus.async.AsyncHandler

private[twitterUtil] trait AsyncHandlerTwitterFutureInstances {
  implicit val handleTwitterUtilFuture: AsyncHandler[Future] =
    new AsyncHandler[Future] {
      def handle[A](callback: AsyncHandler.Callback[A], cancel: => Unit): Future[A] = {
        val p = Promise[A]()

        p.setInterruptHandler {
          case e: Throwable =>
            if (!p.isDefined) {
              p.setException(e)
              cancel
            }
        }

        callback {
          case Left(e)  => p.setException(e)
          case Right(v) => p.setValue(v)
        }

        p
      }
    }
}
