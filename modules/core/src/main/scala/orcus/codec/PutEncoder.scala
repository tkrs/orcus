package orcus.codec

import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.util.Bytes
import shapeless._
import shapeless.labelled.FieldType

trait PutEncoder[A] {
  def apply(acc: Put, a: A, ts: Long): Put
}

object PutEncoder extends PutEncoder1 {

  def apply[A](implicit A: PutEncoder[A]): PutEncoder[A] = A

  implicit def mapPutEncoder[K, V](
      implicit
      K: ValueCodec[K],
      V: Lazy[PutFamilyEncoder[V]]
  ): PutEncoder[Map[K, V]] = new PutEncoder[Map[K, V]] {
    def apply(acc: Put, a: Map[K, V], ts: Long = Long.MaxValue): Put = {
      a.foreach {
        case (k, v) =>
          V.value.apply(acc, K.encode(k), v, ts)
      }
      acc
    }
  }
}

trait PutEncoder1 {

  implicit val hnilPutEncoder: PutEncoder[HNil] = new PutEncoder[HNil] {
    def apply(acc: Put, a: HNil, ts: Long): Put = acc
  }

  implicit def hlabelledConsPutEncoder[K <: Symbol, H, T <: HList](
      implicit
      K: Witness.Aux[K],
      H: Lazy[PutFamilyEncoder[H]],
      T: Lazy[PutEncoder[T]]
  ): PutEncoder[FieldType[K, H] :: T] = new PutEncoder[::[FieldType[K, H], T]] {
    def apply(acc: Put, a: FieldType[K, H] :: T, ts: Long): Put = a match {
      case h :: t =>
        val hp = H.value(acc, Bytes.toBytes(K.value.name), h, ts)
        T.value(hp, t, ts)
    }
  }

  implicit def caseClassPutEncoder[A, R](
      implicit
      gen: LabelledGeneric.Aux[A, R],
      R: Lazy[PutEncoder[R]]
  ): PutEncoder[A] = new PutEncoder[A] {
    def apply(acc: Put, a: A, ts: Long = Long.MaxValue): Put = {
      R.value(acc, gen.to(a), ts)
    }
  }
}
