package orcus.codec

import org.apache.hadoop.hbase.util.Bytes
import shapeless.::
import shapeless.HList
import shapeless.HNil
import shapeless.LabelledGeneric
import shapeless.Lazy
import shapeless.Witness
import shapeless.labelled.FieldType
import shapeless.labelled.field

trait DerivedFamilyDecoder[A] extends FamilyDecoder[A]

object DerivedFamilyDecoder extends DerivedFamilyDecoder1

trait DerivedFamilyDecoder1 {
  implicit val familyDecodeHNil: DerivedFamilyDecoder[HNil] = _ => Right(HNil)

  implicit def familyDecodeLabelledHCons[K <: Symbol, H, T <: HList](implicit
    K: Witness.Aux[K],
    H: ValueCodec[H],
    T: Lazy[DerivedFamilyDecoder[T]]
  ): DerivedFamilyDecoder[FieldType[K, H] :: T] =
    map =>
      T.value(map) match {
        case Right(t) =>
          H.decode(map.get(Bytes.toBytes(K.value.name))) match {
            case Right(h) => Right(field[K](h) :: t)
            case Left(e)  => Left(e)
          }
        case Left(e) => Left(e)
      }

  implicit def familyDecodeLabelledGen[H <: HList, A](implicit
    gen: LabelledGeneric.Aux[A, H],
    A: Lazy[DerivedFamilyDecoder[H]]
  ): DerivedFamilyDecoder[A] =
    map =>
      A.value(map) match {
        case Right(v) => Right(gen.from(v))
        case Left(e)  => Left(e)
      }
}
