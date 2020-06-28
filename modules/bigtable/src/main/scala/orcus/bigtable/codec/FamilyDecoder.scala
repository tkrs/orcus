package orcus.bigtable.codec

import com.google.cloud.bigtable.data.v2.models.RowCell
import orcus.internal.ScalaVersionSpecifics._

import scala.annotation.tailrec

trait FamilyDecoder[A] {
  def apply(family: List[RowCell]): Either[Throwable, A]
}

object FamilyDecoder extends FamilyDecoder1 {
  @inline def apply[A](implicit A: FamilyDecoder[A]): FamilyDecoder[A] = A
}

private[bigtable] trait FamilyDecoder1 {
  implicit def decodeMap[K, V, M[_, _] <: Map[K, V]](implicit
    decodeK: PrimitiveDecoder[K],
    decodeV: PrimitiveDecoder[V],
    factory: Factory[(K, V), M[K, V]]
  ): FamilyDecoder[M[K, V]] = { family =>
    val builder = factory.newBuilder

    @tailrec def loop(cells: List[RowCell]): Either[Throwable, M[K, V]] =
      cells match {
        case h :: t =>
          decodeK(h.getQualifier) match {
            case Right(q) =>
              decodeV(h.getValue) match {
                case Right(v) => builder += q -> v; loop(t)
                case l        => l.asInstanceOf[Either[Throwable, M[K, V]]]
              }
            case l => l.asInstanceOf[Either[Throwable, M[K, V]]]
          }
        case _ =>
          Right(builder.result())
      }

    loop(family)
  }

  implicit def decodeOptionA[A](implicit decodeA: FamilyDecoder[A]): FamilyDecoder[Option[A]] =
    family =>
      if (family == null || family.isEmpty) Right(None)
      else decodeA(family).map(Option.apply)
}
