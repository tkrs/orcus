package orcus.async.monix

import monix.eval.Task
import orcus.async.{AsyncHandler, Callback}

trait MonixAsynHandlerInstances {

  implicit val handleMonixTask: AsyncHandler[Task] = new AsyncHandler[Task] {
    def handle[A](callback: Callback[A], cancel: => Unit): Task[A] = {
      Task
        .cancelable0[A] { (scheduler, cb) =>
          scheduler.execute(new Runnable {
            def run(): Unit = callback(cb)
          })
          Task(cancel)
        }
    }
  }
}
