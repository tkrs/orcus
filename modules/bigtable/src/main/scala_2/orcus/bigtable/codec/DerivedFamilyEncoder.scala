package orcus.bigtable.codec

import shapeless.::
import shapeless.HList
import shapeless.HNil
import shapeless.LabelledGeneric
import shapeless.Lazy
import shapeless.Witness
import shapeless.labelled.FieldType

trait DerivedFamilyEncoder[A] extends FamilyEncoder[A]

object DerivedFamilyEncoder extends DerivedFamilyEncoder1

private[codec] trait DerivedFamilyEncoder1 {

  implicit val encodeFamilyHNil: DerivedFamilyEncoder[HNil] = _ => Map.empty

  implicit def encodeFamilyLabelledHCons[K <: Symbol, H, T <: HList](implicit
    K: Witness.Aux[K],
    encodeH: PrimitiveEncoder[H],
    encodeT: Lazy[DerivedFamilyEncoder[T]]
  ): DerivedFamilyEncoder[FieldType[K, H] :: T] = { a =>
    encodeT.value(a.tail) + (PrimitiveEncoder[String].apply(K.value.name) -> encodeH(a.head))
  }

  implicit def encodeFamilyGen[A, R](implicit
    gen: LabelledGeneric.Aux[A, R],
    encodeR: Lazy[DerivedFamilyEncoder[R]]
  ): DerivedFamilyEncoder[A] = a => encodeR.value(gen.to(a))
}
