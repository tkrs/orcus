package orcus.bigtable

import cats.syntax.traverse._
import com.google.cloud.bigtable.data.v2.models.RowCell
import orcus.bigtable.codec.RowDecoder

final case class CRow(rowKey: String, families: Map[String, List[RowCell]]) {
  def decode[A: RowDecoder]: Either[Throwable, A] = RowDecoder[A].apply(this)
}

object CRow {

  def decodeRows[A: RowDecoder](rows: Vector[CRow]): Either[Throwable, Vector[A]] =
    rows.traverse(_.decode[A])
}
