package orcus.async.catsEffect

import cats.effect.Effect
import orcus.async.{AsyncHandler, Callback}

private[catsEffect] trait AsyncHandlerInstances {

  implicit def handleEffect[F[_]](implicit F: Effect[F]): AsyncHandler[F] =
    new AsyncHandler[F] {
      def handle[A](callback: Callback[A], cancel: => Unit): F[A] =
        F.async(callback)
    }
}
