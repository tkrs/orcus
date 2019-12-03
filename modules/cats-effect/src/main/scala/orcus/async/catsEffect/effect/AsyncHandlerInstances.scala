package orcus.async.catsEffect.effect

import cats.effect.Effect
import orcus.async.{AsyncHandler, Callback}

private[effect] trait AsyncHandlerInstances {
  implicit def handleEffect[F[_]](implicit F: Effect[F]): AsyncHandler[F] =
    new AsyncHandler[F] {
      def handle[A](callback: Callback[A], cancel: => Unit): F[A] =
        F.async(callback)
    }
}
