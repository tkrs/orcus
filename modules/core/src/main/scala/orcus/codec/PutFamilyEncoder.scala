package orcus.codec

import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.util.Bytes
import shapeless._
import shapeless.labelled.FieldType

trait PutFamilyEncoder[A] {
  def apply(acc: Put, cf: Array[Byte], a: A): Put
}

object PutFamilyEncoder extends HighPriorityPutFamilyEncoder {
  @inline def apply[A](implicit A: PutFamilyEncoder[A]): PutFamilyEncoder[A] = A
}

trait HighPriorityPutFamilyEncoder extends LowPriorityPutFamilyEncoder {

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

trait LowPriorityPutFamilyEncoder {

  implicit val encodePutFamlyHNil: PutFamilyEncoder[HNil] = new PutFamilyEncoder[HNil] {
    def apply(acc: Put, cf: Array[Byte], a: HNil): Put = acc
  }

  implicit def encodePutFamlyLabelledHCons[K <: Symbol, H, T <: HList](
    implicit
    K: Witness.Aux[K],
    H: ValueCodec[H],
    T: Lazy[PutFamilyEncoder[T]]
  ): PutFamilyEncoder[FieldType[K, H] :: T] =
    new PutFamilyEncoder[FieldType[K, H] :: T] {

      def apply(acc: Put, cf: Array[Byte], a: FieldType[K, H] :: T): Put =
        T.value(acc, cf, a.tail).addColumn(cf, Bytes.toBytes(K.value.name), H.encode(a.head))
    }

  implicit def encodePutFamlyGen[A, R](
    implicit
    gen: LabelledGeneric.Aux[A, R],
    R: Lazy[PutFamilyEncoder[R]]
  ): PutFamilyEncoder[A] = new PutFamilyEncoder[A] {

    def apply(acc: Put, cf: Array[Byte], a: A): Put =
      R.value(acc, cf, gen.to(a))
  }
}
