package orcus.codec

import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.util.Bytes
import shapeless._
import shapeless.labelled.FieldType

trait PutEncoder[A] {
  def apply(acc: Put, a: A): Put
}

object PutEncoder extends PutEncoder1 {
  @inline def apply[A](implicit A: PutEncoder[A]): PutEncoder[A] = A
}

trait PutEncoder1 {

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
