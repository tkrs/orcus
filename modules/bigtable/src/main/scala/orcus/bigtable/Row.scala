package orcus.bigtable

import cats.syntax.traverse.*
import com.google.cloud.bigtable.data.v2.models.RowCell
import orcus.bigtable.codec.RowDecoder

final case class Row(rowKey: String, families: Map[String, List[RowCell]]) {
  def decode[A: RowDecoder]: Either[Throwable, A] = RowDecoder[A].apply(this)
}

object Row {

  def decodeRows[A: RowDecoder](rows: Vector[Row]): Either[Throwable, Vector[A]] =
    rows.traverse(_.decode[A])
}
