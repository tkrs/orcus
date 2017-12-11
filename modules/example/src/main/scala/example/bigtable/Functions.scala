package example.bigtable

import java.io.Closeable

import cats.data.Kleisli
import cats.~>
import org.apache.hadoop.hbase.client._

import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

object Functions {
  def bracket[R <: Closeable, A](tr: Try[R])(f: R => Try[A]): Try[A] = tr match {
    case Failure(e) =>
      Failure(e)
    case Success(r) =>
      try {
        f(r).map { a =>
          try r.close()
          catch { case NonFatal(_) => () }
          a
        }
      } catch {
        case NonFatal(e) =>
          try r.close()
          catch { case NonFatal(_) => () }
          Failure(e)
      }
  }

  def getAdmin(c: Connection): Try[Admin] = Try(c.getAdmin)

  implicit final class Nat[F[_], G[_]](val nat: F ~> G) extends AnyVal {
    def liftF[E]: F ~> Kleisli[G, E, ?] = λ[F ~> Kleisli[G, E, ?]](fa => Kleisli(_ => nat(fa)))
  }
}
