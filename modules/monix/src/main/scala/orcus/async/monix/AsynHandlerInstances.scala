package orcus.async.monix

import _root_.monix.eval.Task
import orcus.async.{AsyncHandler, Callback}

private[monix] trait AsynHandlerInstances {

  implicit val handleMonixTask: AsyncHandler[Task] = new AsyncHandler[Task] {
    def handle[A](callback: Callback[A], cancel: => Unit): Task[A] =
      Task
        .cancelable0[A] { (scheduler, cb) =>
          scheduler.execute(new Runnable {
            def run(): Unit = callback(cb)
          })
          Task(cancel)
        }
  }
}
