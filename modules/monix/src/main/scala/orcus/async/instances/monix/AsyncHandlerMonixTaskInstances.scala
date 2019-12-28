package orcus.async.instances.monix

import _root_.monix.eval.Task
import orcus.async.AsyncHandler

private[monix] trait AsyncHandlerMonixTaskInstances {
  implicit val handleMonixTask: AsyncHandler[Task] = new AsyncHandler[Task] {
    def handle[A](callback: AsyncHandler.Callback[A], cancel: => Unit): Task[A] =
      Task
        .cancelable0[A] { (scheduler, cb) =>
          scheduler.execute(() => callback(cb))
          Task(cancel)
        }
  }
}
