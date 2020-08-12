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

trait DerivedDecoder[A] extends Decoder[A]

object DerivedDecoder extends DerivedDecoder1

trait DerivedDecoder1 {
  implicit val decodeHNil: DerivedDecoder[HNil] = _ => Right(HNil)

  implicit def decodeLabelledHCons[K <: Symbol, H, T <: HList](implicit
    K: Witness.Aux[K],
    H: FamilyDecoder[H],
    T: Lazy[DerivedDecoder[T]]
  ): DerivedDecoder[FieldType[K, H] :: T] =
    result =>
      T.value(result) match {
        case Right(t) =>
          val k = Bytes.toBytes(K.value.name)
          H(result.getFamilyMap(k)) match {
            case Right(h) => Right(field[K](h) :: t)
            case Left(e)  => Left(e)
          }
        case Left(e) =>
          Left(e)
      }

  implicit def decodeLabelledGen[H <: HList, A](implicit
    gen: LabelledGeneric.Aux[A, H],
    A: Lazy[DerivedDecoder[H]]
  ): DerivedDecoder[A] =
    result =>
      A.value(result) match {
        case Right(v) => Right(gen.from(v))
        case Left(e)  => Left(e)
      }
}
