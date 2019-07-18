package orcus.codec

import org.apache.hadoop.hbase.client.Put

trait PutEncoder[A] {
  def apply(acc: Put, a: A): Put
}

object PutEncoder extends LowPriorityPutEncoder {

  @inline def apply[A](implicit A: PutEncoder[A]): PutEncoder[A] = A
}

trait LowPriorityPutEncoder {

  implicit def encodeMap[K, V](
    implicit
    K: ValueCodec[K],
    V: PutFamilyEncoder[V]
  ): PutEncoder[Map[K, V]] = new PutEncoder[Map[K, V]] {
    def apply(acc: Put, a: Map[K, V]): Put = {
      a.foreach {
        case (k, v) =>
          V(acc, K.encode(k), v)
      }
      acc
    }
  }
}
