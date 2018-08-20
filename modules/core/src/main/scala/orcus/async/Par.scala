package orcus.async

import java.util.concurrent.CompletableFuture

import cats.~>

trait Par[F[_]] {
  def parallel: CompletableFuture ~> F
}

object Par {
  @inline def apply[F[_]](implicit F: Par[F]): Par[F] = F
}
