package orcus.async.twitterutil

import com.twitter.util.{Future, Promise}
import orcus.async.{AsyncHandler, Callback}

trait TwitterUtilAsyncHandlerInstances {

  implicit val twitterUtilFutureAsyncHandler: AsyncHandler[Future] =
    new AsyncHandler[Future] {
      def handle[A](callback: Callback[A], cancel: => Unit): Future[A] = {
        val p = Promise[A]
        callback {
          case Left(e)  => p.setException(e)
          case Right(v) => p.setValue(v)
        }
        p
      }
    }
}
