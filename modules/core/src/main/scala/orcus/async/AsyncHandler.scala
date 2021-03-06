package orcus.async

trait AsyncHandler[F[_]] {
  import AsyncHandler.Callback

  def handle[A](callback: Callback[A], cancel: => Unit): F[A]
}

object AsyncHandler {
  type Callback[A] = (Either[Throwable, A] => Unit) => Unit

  @inline def apply[F[_]](implicit F: AsyncHandler[F]): AsyncHandler[F] = F
}
