package orcus.async.instances.catsEffect

import cats.effect.ConcurrentEffect
import orcus.async.AsyncHandler

import scala.concurrent.ExecutionContext

private[catsEffect] trait AsyncHandlerConcurrentEffectInstances {
  implicit def handleConcurrentEffect[F[_]](implicit F: ConcurrentEffect[F], ec: ExecutionContext): AsyncHandler[F] =
    new AsyncHandler[F] {
      def handle[A](callback: AsyncHandler.Callback[A], cancel: => Unit): F[A] =
        F.cancelable[A] { cb =>
          ec.execute(() => callback(cb))
          F.delay(cancel)
        }
    }
}
