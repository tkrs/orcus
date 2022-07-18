package orcus.example

import orcus.bigtable.codec.DerivedFamilyDecoder
import orcus.bigtable.codec.DerivedRowDecoder

final case class T(c1: C1, c2: C2) derives DerivedRowDecoder
final case class C1(q1: String, q2: List[String]) derives DerivedFamilyDecoder
final case class C2(q1: List[String]) derives DerivedFamilyDecoder
