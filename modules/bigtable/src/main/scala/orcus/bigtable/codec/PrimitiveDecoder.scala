package orcus.bigtable.codec

import com.google.common.primitives.Ints
import com.google.common.primitives.Longs
import com.google.common.primitives.Shorts
import com.google.protobuf.ByteString

import scala.util.control.NonFatal

trait PrimitiveDecoder[A] {
  def apply(value: ByteString): Either[Throwable, A]
}

object PrimitiveDecoder extends PrimitiveDecoder1 {
  @inline def apply[A](implicit A: PrimitiveDecoder[A]): PrimitiveDecoder[A] = A
}

private[bigtable] trait PrimitiveDecoder1 {
  implicit val decodeString: PrimitiveDecoder[String] = bs =>
    try Right(bs.toStringUtf8)
    catch {
      case NonFatal(e) => Left(e)
    }

  implicit val decodeByteString: PrimitiveDecoder[ByteString] = bs =>
    try Right(bs)
    catch {
      case NonFatal(e) => Left(e)
    }

  implicit val decodeLong: PrimitiveDecoder[Long] = bs =>
    try Right(Longs.fromByteArray(bs.toByteArray))
    catch {
      case NonFatal(e) => Left(e)
    }

  implicit val decodeBytes: PrimitiveDecoder[Array[Byte]] = bs =>
    try Right(bs.toByteArray)
    catch {
      case NonFatal(e) => Left(e)
    }

  implicit val decodeBooleanAsHBase: PrimitiveDecoder[Boolean] = bs =>
    try Right(bs.byteAt(0) != 0.toByte)
    catch {
      case NonFatal(e) => Left(e)
    }

  implicit val decodeShortAsHBase: PrimitiveDecoder[Short] = bs =>
    try Right(Shorts.fromByteArray(bs.toByteArray))
    catch {
      case NonFatal(e) => Left(e)
    }

  implicit val decodeIntAsHBase: PrimitiveDecoder[Int] = bs =>
    try Right(Ints.fromByteArray(bs.toByteArray))
    catch {
      case NonFatal(e) => Left(e)
    }

  implicit val decodeFloatAsHBase: PrimitiveDecoder[Float] = bs =>
    try Right(java.lang.Float.intBitsToFloat(Ints.fromByteArray(bs.toByteArray)))
    catch {
      case NonFatal(e) => Left(e)
    }

  implicit val decodeDoubleAsHBase: PrimitiveDecoder[Double] = bs =>
    try Right(java.lang.Double.longBitsToDouble(Longs.fromByteArray(bs.toByteArray)))
    catch {
      case NonFatal(e) => Left(e)
    }

  implicit def decodeOptionA[A](implicit A: PrimitiveDecoder[A]): PrimitiveDecoder[Option[A]] =
    bs =>
      try if (bs == null) Right(None) else A.apply(bs).map(Option.apply)
      catch {
        case NonFatal(e) => Left(e)
      }

  implicit def decodeSomeA[A](implicit A: PrimitiveDecoder[A]): PrimitiveDecoder[Some[A]] =
    bs =>
      try A.apply(bs).map(Some.apply)
      catch {
        case NonFatal(e) => Left(e)
      }

  implicit val decodeNone: PrimitiveDecoder[None.type] = _ =>
    try Right(None)
    catch {
      case NonFatal(e) => Left(e)
    }
}
