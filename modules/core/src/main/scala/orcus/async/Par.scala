package orcus.async

import cats.~>

trait Par[F[_]] {
  type G[_]

  def parallel: F ~> G
}

object Par {

  type Aux[F[_], G0[_]] = Par[F] { type G[α] = G0[α] }

  @inline def apply[F[_], G[_]](implicit F: Par.Aux[F, G]): Par.Aux[F, G] = F
}
