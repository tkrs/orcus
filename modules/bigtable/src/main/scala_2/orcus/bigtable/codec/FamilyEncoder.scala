package orcus.bigtable.codec

import com.google.protobuf.ByteString

trait FamilyEncoder[A] {
  def apply(v: A): Map[ByteString, ByteString]
}

object FamilyEncoder extends FamilyEncoder1 {
  @inline def apply[A](implicit A: FamilyEncoder[A]): FamilyEncoder[A] = A
}

private[bigtable] trait FamilyEncoder1 {

  implicit def encodeMap[K, V, M[_, _] <: Map[K, V]](implicit
    encodeK: PrimitiveEncoder[K],
    encodeV: PrimitiveEncoder[V]
  ): FamilyEncoder[M[K, V]] =
    _.map { case (k, v) => encodeK(k) -> encodeV(v) }
}
