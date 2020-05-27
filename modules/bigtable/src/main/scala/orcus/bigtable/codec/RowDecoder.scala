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

trait RowDecoder1 {
  implicit val decodeRowAsRow: RowDecoder[CRow] = row => Right(row)

  implicit def decodeRowAsMap[V, M[_, _] <: Map[String, V]](implicit
    decodeV: FamilyDecoder[V],
    factory: Factory[(String, V), M[String, V]]
  ): RowDecoder[M[String, V]] =
    row => decodeRow(row)

  implicit def decodeRowAsVWithKey[V](implicit V: RowDecoder[V]): RowDecoder[(String, V)] =
    row => V.apply(row).map(r => row.rowKey -> r)

  private def decodeRow[V, M[_, _] <: Map[String, V]](row: CRow)(implicit
    decodeV: FamilyDecoder[V],
    factory: Factory[(String, V), M[String, V]]
  ): Either[Throwable, M[String, V]] = {
    val builder = factory.newBuilder
    val it = row.families.iterator

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
