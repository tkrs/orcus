package orcus.codec

import org.apache.hadoop.hbase.util.Bytes
import shapeless.::
import shapeless.HList
import shapeless.HNil
import shapeless.LabelledGeneric
import shapeless.Lazy
import shapeless.Witness
import shapeless.labelled.FieldType

trait DerivedPutFamilyEncoder[A] extends PutFamilyEncoder[A]

object DerivedPutFamilyEncoder extends DerivedPutFamilyEncoder1

trait DerivedPutFamilyEncoder1 {
  implicit val encodePutFamlyHNil: DerivedPutFamilyEncoder[HNil] = (acc, _, _) => acc

  implicit def encodePutFamlyLabelledHCons[K <: Symbol, H, T <: HList](implicit
    K: Witness.Aux[K],
    H: ValueCodec[H],
    T: Lazy[DerivedPutFamilyEncoder[T]]
  ): DerivedPutFamilyEncoder[FieldType[K, H] :: T] =
    (acc, cf, a) => T.value(acc, cf, a.tail).addColumn(cf, Bytes.toBytes(K.value.name), H.encode(a.head))

  implicit def encodePutFamlyGen[A, R](implicit
    gen: LabelledGeneric.Aux[A, R],
    R: Lazy[DerivedPutFamilyEncoder[R]]
  ): DerivedPutFamilyEncoder[A] = (acc, cf, a) => R.value(acc, cf, gen.to(a))
}
