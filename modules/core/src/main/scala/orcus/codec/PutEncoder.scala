package orcus.codec

import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.util.Bytes
import shapeless._
import shapeless.labelled.FieldType

trait PutEncoder[A] {
  def apply(acc: Put, a: A): Put
}

object PutEncoder extends PutEncoder1 {

  def apply[A](implicit A: PutEncoder[A]): PutEncoder[A] = A

  implicit def mapPutEncoder[K, V](
      implicit
      K: ValueCodec[K],
      V: PutFamilyEncoder[V]
  ): PutEncoder[Map[K, V]] = new PutEncoder[Map[K, V]] {
    def apply(acc: Put, a: Map[K, V]): Put = {
      a.foreach {
        case (k, v) =>
          V(acc, K.encode(Option(k)), v)
      }
      acc
    }
  }
}

trait PutEncoder1 {

  implicit val hnilPutEncoder: PutEncoder[HNil] = new PutEncoder[HNil] {
    def apply(acc: Put, a: HNil): Put = acc
  }

  implicit def labelledHConsPutEncoder[K <: Symbol, H, T <: HList](
      implicit
      K: Witness.Aux[K],
      H: PutFamilyEncoder[H],
      T: PutEncoder[T]
  ): PutEncoder[FieldType[K, H] :: T] = new PutEncoder[FieldType[K, H] :: T] {
    def apply(acc: Put, a: FieldType[K, H] :: T): Put = a match {
      case h :: t =>
        val hp = H(acc, Bytes.toBytes(K.value.name), h)
        T(hp, t)
    }
  }

  implicit def caseClassPutEncoder[A, R](
      implicit
      gen: LabelledGeneric.Aux[A, R],
      R: Lazy[PutEncoder[R]]
  ): PutEncoder[A] = new PutEncoder[A] {
    def apply(acc: Put, a: A): Put = {
      R.value(acc, gen.to(a))
    }
  }
}
