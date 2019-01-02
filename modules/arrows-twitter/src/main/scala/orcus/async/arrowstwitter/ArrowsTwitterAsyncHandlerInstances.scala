package orcus.async.arrowstwitter

import arrows.twitter.Task
import com.twitter.util.{FuturePool, Promise}
import orcus.async.{AsyncHandler, Callback}

trait ArrowsTwitterAsyncHandlerInstances {

  implicit val arrowsTwitterTaskAsyncHandler: AsyncHandler[Task] =
    new AsyncHandler[Task] {
      def handle[A](callback: Callback[A], cancel: => Unit): Task[A] =
        Task.async {
          val p = Promise[A]
          callback {
            case Left(e)  => p.setException(e)
            case Right(v) => p.setValue(v)
          }
          p
        }
    }

  def forkedTaskAsyncHandler(implicit fp: FuturePool): AsyncHandler[Task] =
    new AsyncHandler[Task] {
      def handle[A](callback: Callback[A], cancel: => Unit): Task[A] =
        Task.fork(fp)(Task.async {
          val p = Promise[A]
          callback {
            case Left(e)  => p.setException(e)
            case Right(v) => p.setValue(v)
          }
          p
        })
    }
}
