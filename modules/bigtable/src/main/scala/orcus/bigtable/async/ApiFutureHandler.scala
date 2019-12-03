package orcus.bigtable.async

import cats.syntax.either._
import cats.~>
import com.google.api.core.{ApiFuture, ApiFutureCallback, ApiFutures}
import com.google.common.util.concurrent.MoreExecutors
import orcus.async.{AsyncHandler, Par}

trait ApiFutureHandler {
  implicit def handleGoogleApiFuture[F[_]](implicit F: AsyncHandler[F]): Par.Aux[ApiFuture, F] = new Par[ApiFuture] {
    type G[α] = F[α]

    def parallel: ApiFuture ~> F = λ[ApiFuture ~> F](toF(_))

    private def toF[A](f: ApiFuture[A]): F[A] =
      F.handle[A](
        { cb =>
          ApiFutures.addCallback(
            f,
            new ApiFutureCallback[A] {
              def onFailure(t: Throwable): Unit = cb(t.asLeft)
              def onSuccess(a: A): Unit         = cb(a.asRight)
            },
            MoreExecutors.directExecutor()
          )
        }, {
          val _ = f.cancel(false)
        }
      )
  }
}
