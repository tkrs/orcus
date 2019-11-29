package orcus.bigtable.codec

import shapeless.Lazy

object auto {
  implicit def autoDerivedFamilyDecoder[A](implicit A: Lazy[DerivedFamilyDecoder[A]]): FamilyDecoder[A] = A.value
  implicit def autoDerivedRowDecoder[A](implicit A: Lazy[DerivedRowDecoder[A]]): RowDecoder[A]          = A.value
}
