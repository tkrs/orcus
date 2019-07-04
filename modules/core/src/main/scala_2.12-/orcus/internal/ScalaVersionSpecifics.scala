package orcus.internal

import scala.collection.generic.CanBuildFrom
import scala.collection.mutable

private[orcus] object ScalaVersionSpecifics {
  private[orcus] type Factory[-E, +T] = CanBuildFrom[Nothing, E, T]

  implicit private[orcus] class FactoryOps[F, E, T](val bf: Factory[E, T]) extends AnyVal {
    def newBuilder: mutable.Builder[E, T] = bf.apply()
  }
}
