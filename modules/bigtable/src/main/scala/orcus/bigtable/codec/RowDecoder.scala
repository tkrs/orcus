package orcus.bigtable.codec

import orcus.bigtable.CRow
import orcus.internal.ScalaVersionSpecifics._

import scala.annotation.tailrec

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
