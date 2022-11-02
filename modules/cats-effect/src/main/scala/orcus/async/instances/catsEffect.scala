package orcus.async.instances

import cats.effect.Async
import orcus.async.AsyncHandler

object catsEffect {

  implicit def handleAsync[F[_]](implicit F: Async[F]): AsyncHandler[F] =
    new AsyncHandler[F] {
      def handle[A](callback: AsyncHandler.Callback[A], cancel: => Unit): F[A] =
        F.async[A] { cb => callback(cb); F.pure(Some(F.delay(cancel))) }
    }
}
