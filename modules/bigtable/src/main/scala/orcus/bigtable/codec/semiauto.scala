package orcus.bigtable.codec

import shapeless.Lazy

object semiauto {
  def derivedFamilyDecoder[A](implicit A: Lazy[DerivedFamilyDecoder[A]]): FamilyDecoder[A] = A.value
  def derivedRowDecoder[A](implicit A: Lazy[DerivedRowDecoder[A]]): RowDecoder[A] = A.value
}
