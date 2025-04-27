package orcus.example

import orcus.bigtable.codec.*
import orcus.bigtable.codec.semiauto.*

final case class T(c1: C1, c2: C2)
object T {
  implicit val decode: RowDecoder[T] = derivedRowDecoder
}
final case class C1(q1: String, q2: List[String])
object C1 {
  implicit val decode: FamilyDecoder[C1] = derivedFamilyDecoder
}
final case class C2(q1: List[String])
object C2 {
  implicit val decode: FamilyDecoder[C2] = derivedFamilyDecoder
}
