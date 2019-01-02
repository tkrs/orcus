package orcus.async.catseffect.concurrent

import cats.effect.ConcurrentEffect
import orcus.async.{AsyncHandler, Callback}

import scala.concurrent.ExecutionContext

trait CatsEffectConcurrentHandlerInstances {

  implicit def handleConcurrentEffect[F[_]](implicit F: ConcurrentEffect[F], ec: ExecutionContext): AsyncHandler[F] =
    new AsyncHandler[F] {
      def handle[A](callback: Callback[A], cancel: => Unit): F[A] =
        F.cancelable[A] { cb =>
          ec.execute(new Runnable { def run(): Unit = callback(cb) })
          F.delay(cancel)
        }
    }
}
