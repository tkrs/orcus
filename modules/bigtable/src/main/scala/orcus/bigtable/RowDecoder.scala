package orcus.bigtable

import com.google.cloud.bigtable.data.v2.models.RowCell
import com.google.protobuf.ByteString
import orcus.internal.ScalaVersionSpecifics._
import org.apache.hadoop.hbase.util.Bytes

import scala.annotation.tailrec
import scala.util.control.NonFatal

trait RowDecoder[A] {
  def apply(row: CRow): Either[Throwable, A]
}

object RowDecoder extends RowDecoder1 {
  @inline def apply[A](implicit A: RowDecoder[A]): RowDecoder[A] = A
}

trait RowDecoder1 extends RowDecoder2 {

  implicit val decodeRowAsRow: RowDecoder[CRow] = row => Right(row)

  implicit def decodeRowAsMap[V, M[_, _] <: Map[String, V]](
    implicit
    decodeV: FamilyDecoder[V],
    factory: Factory[(String, V), M[String, V]]
  ): RowDecoder[M[String, V]] =
    row => decodeRow(row)

  implicit def decodeRowAsVWithKey[V](implicit V: RowDecoder[V]): RowDecoder[(String, V)] =
    row => V.apply(row).map(r => row.rowKey -> r)

  private def decodeRow[V, M[_, _] <: Map[String, V]](row: CRow)(
    implicit
    decodeV: FamilyDecoder[V],
    factory: Factory[(String, V), M[String, V]]
  ): Either[Throwable, M[String, V]] = {
    val builder = factory.newBuilder
    val it      = row.families.iterator

    @tailrec def loop(): Either[Throwable, M[String, V]] =
      if (!it.hasNext) Right(builder.result())
      else {
        val (k, v) = it.next()
        decodeV.apply(v) match {
          case Right(v) =>
            builder += k -> v
            loop()
          case l => l.asInstanceOf[Either[Throwable, M[String, V]]]
        }
      }

    loop()
  }
}

trait RowDecoder2 {

  import shapeless._
  import shapeless.labelled._

  implicit val decodeHNil: RowDecoder[HNil] = _ => Right(HNil)

  implicit def decodeLabelledHCons[K <: Symbol, H, T <: HList](
    implicit
    K: Witness.Aux[K],
    H: FamilyDecoder[H],
    T: Lazy[RowDecoder[T]]
  ): RowDecoder[FieldType[K, H] :: T] =
    row =>
      T.value(row) match {
        case Right(t) =>
          H(row.families.getOrElse(K.value.name, null)) match {
            case Right(h) => Right(field[K](h) :: t)
            case Left(e)  => Left(e)
          }
        case Left(e) =>
          Left(e)
      }

  implicit def decodeLabelledGen[H <: HList, A](
    implicit
    gen: LabelledGeneric.Aux[A, H],
    A: Lazy[RowDecoder[H]]
  ): RowDecoder[A] =
    result =>
      A.value(result) match {
        case Right(v) => Right(gen.from(v))
        case Left(e)  => Left(e)
      }
}

trait FamilyDecoder[A] {
  def apply(family: List[RowCell]): Either[Throwable, A]
}

object FamilyDecoder extends FamilyDecoder1 {
  @inline def apply[A](implicit A: FamilyDecoder[A]): FamilyDecoder[A] = A
}

trait FamilyDecoder1 extends FamilyDecoder2 {

  implicit def decodeMap[K, V, M[_, _] <: Map[K, V]](
    implicit
    decodeK: PrimitiveDecoder[K],
    decodeV: PrimitiveDecoder[V],
    factory: Factory[(K, V), M[K, V]]
  ): FamilyDecoder[M[K, V]] =
    family => {
      val builder = factory.newBuilder

      @tailrec def loop(cells: List[RowCell]): Either[Throwable, M[K, V]] =
        cells match {
          case h :: t =>
            decodeK.apply(h.getQualifier) match {
              case Right(q) =>
                decodeV.apply(h.getValue) match {
                  case Right(v) =>
                    builder += q -> v
                    loop(t)
                  case l => l.asInstanceOf[Either[Throwable, M[K, V]]]
                }
              case l => l.asInstanceOf[Either[Throwable, M[K, V]]]
            }
          case _ =>
            Right(builder.result())
        }

      loop(family)
    }

  implicit def decodeOptionA[A](
    implicit
    A: FamilyDecoder[A]
  ): FamilyDecoder[Option[A]] =
    family => if (family == null || family.isEmpty) Right(None) else A.apply(family).map(Option.apply)
}

trait FamilyDecoder2 {

  import shapeless._
  import shapeless.labelled._

  implicit val familyDecodeHNil: FamilyDecoder[HNil] = _ => Right(HNil)

  implicit def familyDecodeLabelledHCons[K <: Symbol, H, T <: HList](
    implicit
    K: Witness.Aux[K],
    H: PrimitiveDecoder[H],
    T: Lazy[FamilyDecoder[T]]
  ): FamilyDecoder[FieldType[K, H] :: T] =
    family =>
      T.value(family) match {
        case Right(t) =>
          val h = family.collectFirst {
            case r if r.getQualifier.toStringUtf8 == K.value.name => r.getValue
          }.orNull
          H(h) match {
            case Right(h) => Right(field[K](h) :: t)
            case Left(e)  => Left(e)
          }
        case Left(e) => Left(e)
      }

  implicit def familyDecodeLabelledGen[H <: HList, A](
    implicit
    gen: LabelledGeneric.Aux[A, H],
    A: Lazy[FamilyDecoder[H]]
  ): FamilyDecoder[A] =
    family =>
      A.value(family) match {
        case Right(v) => Right(gen.from(v))
        case Left(e)  => Left(e)
      }
}

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
