package orcus.codec

import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.util.Bytes
import shapeless._
import shapeless.labelled.FieldType

trait PutFamilyEncoder[A] {
  def apply(acc: Put, cf: Array[Byte], a: A): Put
}

object PutFamilyEncoder extends PutFamilyEncoder1 {

  def apply[A](implicit A: PutFamilyEncoder[A]): PutFamilyEncoder[A] = A

}

private[codec] trait PutFamilyEncoder1 extends PutFamilyEncoder2 {

  implicit def encodeMap[K, V](
      implicit
      H: ValueCodec[K],
      V: ValueCodec[V]
  ): PutFamilyEncoder[Map[K, V]] = new PutFamilyEncoder[Map[K, V]] {
    def apply(acc: Put, cf: Array[Byte], a: Map[K, V]): Put = {
      a.foreach {
        case (k, v) =>
          acc.addColumn(cf, H.encode(k), V.encode(v))
      }
      acc
    }
  }
}

private[codec] trait PutFamilyEncoder2 {

  implicit val encodeHNil: PutFamilyEncoder[HNil] = new PutFamilyEncoder[HNil] {
    def apply(acc: Put, cf: Array[Byte], a: HNil): Put = acc
  }

  implicit def encodeLabelledHCons[K <: Symbol, H, T <: HList](
      implicit
      K: Witness.Aux[K],
      H: ValueCodec[H],
      T: PutFamilyEncoder[T]
  ): PutFamilyEncoder[FieldType[K, H] :: T] = new PutFamilyEncoder[FieldType[K, H] :: T] {
    def apply(acc: Put, cf: Array[Byte], a: FieldType[K, H] :: T): Put = {
      T(acc, cf, a.tail).addColumn(cf, Bytes.toBytes(K.value.name), H.encode(a.head))
    }
  }

  implicit def encodeFamilyCaseClass[A, R](
      implicit
      gen: LabelledGeneric.Aux[A, R],
      R: Lazy[PutFamilyEncoder[R]]
  ): PutFamilyEncoder[A] = new PutFamilyEncoder[A] {
    def apply(acc: Put, cf: Array[Byte], a: A): Put = {
      R.value(acc, cf, gen.to(a))
    }
  }
}
