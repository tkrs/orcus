package orcus.bigtable.codec

import com.google.common.primitives.Ints
import com.google.common.primitives.Longs
import com.google.common.primitives.Shorts
import com.google.protobuf.ByteString

trait PrimitiveEncoder[A] {
  def apply(value: A): ByteString
}

object PrimitiveEncoder extends PrimitiveEncoder1 {
  @inline def apply[A](implicit A: PrimitiveEncoder[A]): PrimitiveEncoder[A] = A
}

private[bigtable] trait PrimitiveEncoder1 {
  implicit val encodeString: PrimitiveEncoder[String]         = ByteString.copyFromUtf8(_)
  implicit val encodeByteString: PrimitiveEncoder[ByteString] = identity(_)
  implicit val encodeLong: PrimitiveEncoder[Long]             = v => ByteString.copyFrom(Longs.toByteArray(v))
  implicit val encodeBytes: PrimitiveEncoder[Array[Byte]]     = ByteString.copyFrom(_)
  implicit val encodeBooleanAsHBase: PrimitiveEncoder[Boolean] = v =>
    ByteString.copyFrom(Array(if (v) -1.toByte else 0.toByte))
  implicit val encodeShortAsHBase: PrimitiveEncoder[Short] = v => ByteString.copyFrom(Shorts.toByteArray(v))
  implicit val encodeIntAsHBase: PrimitiveEncoder[Int]     = v => ByteString.copyFrom(Ints.toByteArray(v))
  implicit val encodeFloatAsHBase: PrimitiveEncoder[Float] = v =>
    ByteString.copyFrom(Ints.toByteArray(java.lang.Float.floatToIntBits(v)))
  implicit val encodeDoubleAsHBase: PrimitiveEncoder[Double] = v =>
    ByteString.copyFrom(Longs.toByteArray(java.lang.Double.doubleToLongBits(v)))
  implicit def encodeOptionA[A](implicit encodeA: PrimitiveEncoder[A]): PrimitiveEncoder[Option[A]] = {
    case Some(v) => encodeA(v)
    case _       => ByteString.EMPTY
  }
}
