package orcus.async.catsEffect.concurrent

import cats.effect.ConcurrentEffect
import orcus.async.{AsyncHandler, Callback}

import scala.concurrent.ExecutionContext

private[concurrent] trait AsyncHandlerInstances {

  implicit def handleConcurrentEffect[F[_]](implicit F: ConcurrentEffect[F], ec: ExecutionContext): AsyncHandler[F] =
    new AsyncHandler[F] {

      def handle[A](callback: Callback[A], cancel: => Unit): F[A] =
        F.cancelable[A] { cb =>
          ec.execute(new Runnable { def run(): Unit = callback(cb) })
          F.delay(cancel)
        }
    }
}
