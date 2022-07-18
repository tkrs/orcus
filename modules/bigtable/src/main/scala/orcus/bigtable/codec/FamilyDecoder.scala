package orcus.bigtable.codec

import com.google.cloud.bigtable.data.v2.models.RowCell
import com.google.protobuf.ByteString
import orcus.internal.ScalaVersionSpecifics._

import scala.annotation.tailrec

trait FamilyDecoder[A] {
  def apply(family: List[RowCell]): Either[Throwable, A]
}

object FamilyDecoder {
  @inline def apply[A](implicit A: FamilyDecoder[A]): FamilyDecoder[A] = A

  implicit def decodeMap[K, Q](implicit
    decodeK: PrimitiveDecoder[K],
    decodeV: ValueDecoder[Q],
    factory: Factory[(K, Q), Map[K, Q]]
  ): FamilyDecoder[Map[K, Q]] = { family =>
    val builder = factory.newBuilder

    @tailrec def loop(cells: List[(ByteString, List[RowCell])]): Either[Throwable, Map[K, Q]] =
      cells match {
        case (q, vs) :: t =>
          decodeK(q) match {
            case Right(q) =>
              decodeV(vs) match {
                case Right(v) => builder += q -> v; loop(t)
                case l        => l.asInstanceOf[Either[Throwable, Map[K, Q]]]
              }
            case l => l.asInstanceOf[Either[Throwable, Map[K, Q]]]
          }
        case _ =>
          Right(builder.result())
      }

    if (family == null) Right(builder.result())
    else loop(family.groupBy(_.getQualifier()).toList)
  }

  implicit def decodeOptionA[A](implicit decodeA: FamilyDecoder[A]): FamilyDecoder[Option[A]] =
    family =>
      if (family == null || family.isEmpty) Right(None)
      else decodeA(family).map(Option.apply)
}
