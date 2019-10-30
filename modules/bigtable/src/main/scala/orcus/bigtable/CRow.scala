package orcus.bigtable

import com.google.cloud.bigtable.data.v2.models.RowCell

final case class CRow(rowKey: String, families: Map[String, List[RowCell]])
