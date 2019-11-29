package orcus.bigtable.codec

import com.google.protobuf.ByteString
import org.apache.hadoop.hbase.util.Bytes

import scala.util.control.NonFatal

trait PrimitiveDecoder[A] {
  def apply(value: ByteString): Either[Throwable, A]
}

object PrimitiveDecoder extends PrimitiveDecoder1 {
  @inline def apply[A](implicit A: PrimitiveDecoder[A]): PrimitiveDecoder[A] = A
}

trait PrimitiveDecoder1 {

  implicit val decodeString: PrimitiveDecoder[String] = bs =>
    try Right(bs.toStringUtf8)
    catch {
      case NonFatal(e) => Left(e)
    }

  implicit val decodeBoolean: PrimitiveDecoder[Boolean] = bs =>
    try Right(Bytes.toBoolean(bs.toByteArray))
    catch {
      case NonFatal(e) => Left(e)
    }

  implicit val decodeByteString: PrimitiveDecoder[ByteString] = bs =>
    try Right(bs)
    catch {
      case NonFatal(e) => Left(e)
    }

  implicit val decodeShort: PrimitiveDecoder[Short] = bs =>
    try Right(Bytes.toShort(bs.toByteArray))
    catch {
      case NonFatal(e) => Left(e)
    }

  implicit val decodeInt: PrimitiveDecoder[Int] = bs =>
    try Right(Bytes.toInt(bs.toByteArray))
    catch {
      case NonFatal(e) => Left(e)
    }

  implicit val decodeLong: PrimitiveDecoder[Long] = bs =>
    try Right(Bytes.toLong(bs.toByteArray))
    catch {
      case NonFatal(e) => Left(e)
    }

  implicit val decodeFloat: PrimitiveDecoder[Float] = bs =>
    try Right(Bytes.toFloat(bs.toByteArray))
    catch {
      case NonFatal(e) => Left(e)
    }

  implicit val decodeDouble: PrimitiveDecoder[Double] = bs =>
    try Right(Bytes.toDouble(bs.toByteArray))
    catch {
      case NonFatal(e) => Left(e)
    }

  implicit val decodeBigDecimal: PrimitiveDecoder[BigDecimal] = bs =>
    try Right(Bytes.toBigDecimal(bs.toByteArray))
    catch {
      case NonFatal(e) => Left(e)
    }

  implicit val decodeBytes: PrimitiveDecoder[Array[Byte]] = bs =>
    try Right(bs.toByteArray)
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
