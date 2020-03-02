package orcus.async.instances.catsEffect

import cats.effect.ConcurrentEffect
import orcus.async.AsyncHandler

private[catsEffect] trait AsyncHandlerConcurrentEffectInstances {
  implicit def handleConcurrentEffect[F[_]](implicit F: ConcurrentEffect[F]): AsyncHandler[F] =
    new AsyncHandler[F] {
      def handle[A](callback: AsyncHandler.Callback[A], cancel: => Unit): F[A] =
        F.cancelable[A] { cb =>
          callback(cb)
          F.delay(cancel)
        }
    }
}
