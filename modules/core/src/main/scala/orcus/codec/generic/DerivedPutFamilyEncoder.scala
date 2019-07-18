package orcus.codec
package generic

import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.util.Bytes
import shapeless._
import shapeless.labelled.FieldType

trait DerivedPutFamilyEncoder[A] extends PutFamilyEncoder[A]

object DerivedPutFamilyEncoder extends DerivedPutFamilyEncoder1

private[codec] trait DerivedPutFamilyEncoder1 {

  implicit val encodePutFamlyHNil: DerivedPutFamilyEncoder[HNil] = new DerivedPutFamilyEncoder[HNil] {
    def apply(acc: Put, cf: Array[Byte], a: HNil): Put = acc
  }

  implicit def encodePutFamlyLabelledHCons[K <: Symbol, H, T <: HList](
    implicit
    K: Witness.Aux[K],
    H: ValueCodec[H],
    T: DerivedPutFamilyEncoder[T]
  ): DerivedPutFamilyEncoder[FieldType[K, H] :: T] =
    new DerivedPutFamilyEncoder[FieldType[K, H] :: T] {
      def apply(acc: Put, cf: Array[Byte], a: FieldType[K, H] :: T): Put =
        T(acc, cf, a.tail).addColumn(cf, Bytes.toBytes(K.value.name), H.encode(a.head))
    }

  implicit def encodePutFamlyGen[A, R](
    implicit
    gen: LabelledGeneric.Aux[A, R],
    R: Lazy[DerivedPutFamilyEncoder[R]]
  ): DerivedPutFamilyEncoder[A] = new DerivedPutFamilyEncoder[A] {
    def apply(acc: Put, cf: Array[Byte], a: A): Put =
      R.value(acc, cf, gen.to(a))
  }
}
