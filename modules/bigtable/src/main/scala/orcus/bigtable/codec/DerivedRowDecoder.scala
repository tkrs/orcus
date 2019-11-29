package orcus.bigtable.codec

import shapeless._
import shapeless.labelled._

trait DerivedRowDecoder[A] extends RowDecoder[A]

object DerivedRowDecoder extends DerivedRowDecoder1

trait DerivedRowDecoder1 {
  implicit val decodeHNil: DerivedRowDecoder[HNil] = _ => Right(HNil)

  implicit def decodeLabelledHCons[K <: Symbol, H, T <: HList](
    implicit
    K: Witness.Aux[K],
    H: FamilyDecoder[H],
    T: Lazy[DerivedRowDecoder[T]]
  ): DerivedRowDecoder[FieldType[K, H] :: T] =
    row =>
      T.value(row) match {
        case Right(t) =>
          H(row.families.getOrElse(K.value.name, null)) match {
            case Right(h) => Right(field[K](h) :: t)
            case Left(e)  => Left(e)
          }
        case Left(e) =>
          Left(e)
      }

  implicit def decodeLabelledGen[H <: HList, A](
    implicit
    gen: LabelledGeneric.Aux[A, H],
    A: Lazy[DerivedRowDecoder[H]]
  ): DerivedRowDecoder[A] =
    result =>
      A.value(result) match {
        case Right(v) => Right(gen.from(v))
        case Left(e)  => Left(e)
      }
}
