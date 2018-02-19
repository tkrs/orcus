package orcus.async

trait AsyncHandler[F[_]] {
  def handle[A](callback: Callback[A], cancel: => Unit): F[A]
}

object AsyncHandler extends ScalaFutureAsyncHandlerInstances {

  def apply[F[_]](implicit F: AsyncHandler[F]): AsyncHandler[F] = F
}
