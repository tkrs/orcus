package orcus.async.arrowsTwitter.future

import arrows.twitter.Task
import com.twitter.util.Promise
import orcus.async.{AsyncHandler, Callback}

private[future] trait AsyncHandlerInstances {

  implicit val handleArrowsTwitterFutureTask: AsyncHandler[Task] =
    new AsyncHandler[Task] {
      def handle[A](callback: Callback[A], cancel: => Unit): Task[A] =
        Task.async {
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
