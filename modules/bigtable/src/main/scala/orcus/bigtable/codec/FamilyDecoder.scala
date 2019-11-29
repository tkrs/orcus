package orcus.bigtable.codec

import com.google.cloud.bigtable.data.v2.models.RowCell
import orcus.internal.ScalaVersionSpecifics.Factory

import scala.annotation.tailrec

trait FamilyDecoder[A] {
  def apply(family: List[RowCell]): Either[Throwable, A]
}

object FamilyDecoder extends FamilyDecoder1 {
  @inline def apply[A](implicit A: FamilyDecoder[A]): FamilyDecoder[A] = A
}

trait FamilyDecoder1 extends FamilyDecoder2 {

  implicit def decodeMap[K, V, M[_, _] <: Map[K, V]](
    implicit
    decodeK: PrimitiveDecoder[K],
    decodeV: PrimitiveDecoder[V],
    factory: Factory[(K, V), M[K, V]]
  ): FamilyDecoder[M[K, V]] =
    family => {
      val builder = factory.newBuilder

      @tailrec def loop(cells: List[RowCell]): Either[Throwable, M[K, V]] =
        cells match {
          case h :: t =>
            decodeK.apply(h.getQualifier) match {
              case Right(q) =>
                decodeV.apply(h.getValue) match {
                  case Right(v) =>
                    builder += q -> v
                    loop(t)
                  case l => l.asInstanceOf[Either[Throwable, M[K, V]]]
                }
              case l => l.asInstanceOf[Either[Throwable, M[K, V]]]
            }
          case _ =>
            Right(builder.result())
        }

      loop(family)
    }

  implicit def decodeOptionA[A](
    implicit
    A: FamilyDecoder[A]
  ): FamilyDecoder[Option[A]] =
    family => if (family == null || family.isEmpty) Right(None) else A.apply(family).map(Option.apply)
}

trait FamilyDecoder2 {
  import shapeless._
  import shapeless.labelled._

  implicit val familyDecodeHNil: FamilyDecoder[HNil] = _ => Right(HNil)

  implicit def familyDecodeLabelledHCons[K <: Symbol, H, T <: HList](
    implicit
    K: Witness.Aux[K],
    H: PrimitiveDecoder[H],
    T: Lazy[FamilyDecoder[T]]
  ): FamilyDecoder[FieldType[K, H] :: T] =
    family =>
      T.value(family) match {
        case Right(t) =>
          val h = family.collectFirst {
            case r if r.getQualifier.toStringUtf8 == K.value.name => r.getValue
          }.orNull
          H(h) match {
            case Right(h) => Right(field[K](h) :: t)
            case Left(e)  => Left(e)
          }
        case Left(e) => Left(e)
      }

  implicit def familyDecodeLabelledGen[H <: HList, A](
    implicit
    gen: LabelledGeneric.Aux[A, H],
    A: Lazy[FamilyDecoder[H]]
  ): FamilyDecoder[A] =
    family =>
      A.value(family) match {
        case Right(v) => Right(gen.from(v))
        case Left(e)  => Left(e)
      }
}
