package orcus.async.instances.catsEffect

import cats.effect.Effect
import orcus.async.AsyncHandler

private[catsEffect] trait AsyncHandlerEffectInstances {
  implicit def handleEffect[F[_]](implicit F: Effect[F]): AsyncHandler[F] =
    new AsyncHandler[F] {
      def handle[A](callback: AsyncHandler.Callback[A], cancel: => Unit): F[A] =
        F.async(callback)
    }
}
