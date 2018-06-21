package example

import java.io.Closeable

import cats.data.Kleisli
import cats.{MonadError, ~>}

import scala.util.control.NonFatal

object Functions {
  def bracket[R <: Closeable, F[_], A](tr: F[R])(f: R => F[A])(implicit F: MonadError[F, Throwable]): F[A] = {
    F.flatMap(tr) { r =>
      try f(r)
      catch {
        case NonFatal(e) =>
          try r.close()
          catch {
            case NonFatal(_) => ()
          }
          F.raiseError(e)
      }
    }
  }

  implicit final class Nat[F[_], G[_]](val nat: F ~> G) extends AnyVal {
    def liftF[E]: F ~> Kleisli[G, E, ?] = λ[F ~> Kleisli[G, E, ?]](fa => Kleisli(_ => nat(fa)))
  }
}
