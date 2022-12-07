package orcus.bigtable.codec

import orcus.bigtable.Row
import orcus.internal.ScalaVersionSpecifics._

import scala.annotation.tailrec

trait RowDecoder[A] {
  def apply(row: Row): Either[Throwable, A]
}

object RowDecoder extends RowDecoder1 {
  @inline def apply[A](implicit A: RowDecoder[A]): RowDecoder[A] = A

  implicit val decodeRowAsRow: RowDecoder[Row] = row => Right(row)

  implicit def decodeRowAsMap[V](implicit
    decodeV: FamilyDecoder[V],
    factory: Factory[(String, V), Map[String, V]]
  ): RowDecoder[Map[String, V]] =
    row => decodeRow(row)

  implicit def decodeRowAsVWithKey[V](implicit V: RowDecoder[V]): RowDecoder[(String, V)] =
    row => V.apply(row).map(r => row.rowKey -> r)

  private def decodeRow[V](row: Row)(implicit
    decodeV: FamilyDecoder[V],
    factory: Factory[(String, V), Map[String, V]]
  ): Either[Throwable, Map[String, V]] = {
    val builder = factory.newBuilder
    val it      = row.families.iterator

    @tailrec def loop(): Either[Throwable, Map[String, V]] =
      if (!it.hasNext) Right(builder.result())
      else {
        val (k, v) = it.next()
        decodeV.apply(v) match {
          case Right(v) => builder += k -> v; loop()
          case l        => l.asInstanceOf[Either[Throwable, Map[String, V]]]
        }
      }

    loop()
  }
}
