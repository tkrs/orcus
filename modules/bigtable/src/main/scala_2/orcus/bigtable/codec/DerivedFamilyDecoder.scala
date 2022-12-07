package orcus.bigtable.codec

import shapeless._
import shapeless.labelled._

trait DerivedFamilyDecoder[A] extends FamilyDecoder[A]

object DerivedFamilyDecoder extends DerivedFamilyDecoder1

private[codec] trait DerivedFamilyDecoder1 {

  implicit val familyDecodeHNil: DerivedFamilyDecoder[HNil] = _ => Right(HNil)

  implicit def familyDecodeLabelledHCons[K <: Symbol, H, T <: HList](implicit
    K: Witness.Aux[K],
    H: ValueDecoder[H],
    T: Lazy[DerivedFamilyDecoder[T]]
  ): DerivedFamilyDecoder[FieldType[K, H] :: T] =
    family =>
      T.value(family) match {
        case Right(t) =>
          val h = family.collect {
            case r if r.getQualifier.toStringUtf8 == K.value.name => r
          }
          H(h) match {
            case Right(h) => Right(field[K](h) :: t)
            case Left(e)  => Left(e)
          }
        case Left(e) => Left(e)
      }

  implicit def familyDecodeLabelledGen[H <: HList, A](implicit
    gen: LabelledGeneric.Aux[A, H],
    A: Lazy[DerivedFamilyDecoder[H]]
  ): DerivedFamilyDecoder[A] =
    family =>
      A.value(family) match {
        case Right(v) => Right(gen.from(v))
        case Left(e)  => Left(e)
      }
}
