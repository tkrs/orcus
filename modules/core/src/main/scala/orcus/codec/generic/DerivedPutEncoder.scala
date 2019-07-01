package orcus.codec
package generic

import export.exports
import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.util.Bytes
import shapeless._
import shapeless.labelled.FieldType

trait DerivedPutEncoder[A] extends PutEncoder[A]

@exports
object DerivedPutEncoder extends DerivedPutEncoder1

private[codec] trait DerivedPutEncoder1 {

  implicit val encodeHNil: DerivedPutEncoder[HNil] = new DerivedPutEncoder[HNil] {
    def apply(acc: Put, a: HNil): Put = acc
  }

  implicit def encodeLabelledHCons[K <: Symbol, H, T <: HList](
    implicit
    K: Witness.Aux[K],
    H: PutFamilyEncoder[H],
    T: DerivedPutEncoder[T]
  ): DerivedPutEncoder[FieldType[K, H] :: T] = new DerivedPutEncoder[FieldType[K, H] :: T] {
    def apply(acc: Put, a: FieldType[K, H] :: T): Put =
      H(T(acc, a.tail), Bytes.toBytes(K.value.name), a.head)
  }

  implicit def encodeLabelledGen[A, R](
    implicit
    gen: LabelledGeneric.Aux[A, R],
    R: Lazy[DerivedPutEncoder[R]]
  ): DerivedPutEncoder[A] =
    new DerivedPutEncoder[A] {
      def apply(acc: Put, a: A): Put =
        R.value(acc, gen.to(a))
    }
}
