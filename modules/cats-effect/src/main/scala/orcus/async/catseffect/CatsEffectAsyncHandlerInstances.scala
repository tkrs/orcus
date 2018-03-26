package orcus.async.catseffect

import cats.effect.Async
import orcus.async.{AsyncHandler, Callback}

trait CatsEffectAsyncHandlerInstances {

  implicit def handleCatsEffectAsync[F[_]](implicit F: Async[F]): AsyncHandler[F] =
    new AsyncHandler[F] {
      def handle[A](callback: Callback[A], cancel: => Unit): F[A] = F.async(callback)
    }
}
