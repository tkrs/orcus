package orcus.async.twitterUtil.future

import com.twitter.util.{Future, Promise}
import orcus.async.{AsyncHandler, Callback}

private[future] trait AsyncHandlerInstances {

  implicit val handleTwitterUtilFuture: AsyncHandler[Future] =
    new AsyncHandler[Future] {

      def handle[A](callback: Callback[A], cancel: => Unit): Future[A] = {
        val p = Promise[A]

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
