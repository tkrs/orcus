package orcus.example

import orcus.bigtable.codec.FamilyDecoder
import orcus.bigtable.codec.RowDecoder

final case class T(c1: C1, c2: C2) derives RowDecoder
final case class C1(q1: String, q2: List[String]) derives FamilyDecoder
final case class C2(q1: List[String]) derives FamilyDecoder
