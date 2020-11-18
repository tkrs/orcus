package orcus.codec

import org.apache.hadoop.hbase.util.Bytes
import shapeless.::
import shapeless.HList
import shapeless.HNil
import shapeless.LabelledGeneric
import shapeless.Lazy
import shapeless.Witness
import shapeless.labelled.FieldType

trait DerivedPutEncoder[A] extends PutEncoder[A]

object DerivedPutEncoder extends DerivedPutEncoder1

trait DerivedPutEncoder1 {
  implicit val encodeHNil: DerivedPutEncoder[HNil] = (acc, _) => acc

  implicit def encodeLabelledHCons[K <: Symbol, H, T <: HList](implicit
    K: Witness.Aux[K],
    H: PutFamilyEncoder[H],
    T: Lazy[DerivedPutEncoder[T]]
  ): DerivedPutEncoder[FieldType[K, H] :: T] =
    (acc, a) => H(T.value(acc, a.tail), Bytes.toBytes(K.value.name), a.head)

  implicit def encodeLabelledGen[A, R](implicit
    gen: LabelledGeneric.Aux[A, R],
    R: Lazy[DerivedPutEncoder[R]]
  ): DerivedPutEncoder[A] =
    (acc, a) => R.value(acc, gen.to(a))
}
