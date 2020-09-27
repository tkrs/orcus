package orcus.codec

import org.apache.hadoop.hbase.client.Put

trait PutFamilyEncoder[A] {
  def apply(acc: Put, cf: Array[Byte], a: A): Put
}

object PutFamilyEncoder extends PutFamilyEncoder1 {
  @inline def apply[A](implicit A: PutFamilyEncoder[A]): PutFamilyEncoder[A] = A
}

trait PutFamilyEncoder1 {
  implicit def encodeMap[K, V](implicit
    H: ValueCodec[K],
    V: ValueCodec[V]
  ): PutFamilyEncoder[Map[K, V]] =
    new PutFamilyEncoder[Map[K, V]] {
      def apply(acc: Put, cf: Array[Byte], a: Map[K, V]): Put = {
        a.foreach { case (k, v) =>
          acc.addColumn(cf, H.encode(k), V.encode(v))
        }
        acc
      }
    }
}
