package orcus.bigtable.codec

object auto {
  implicit def autoDerivedFamilyDecoder[A](implicit A: => DerivedFamilyDecoder[A]): FamilyDecoder[A] = A
  implicit def autoDerivedRowDecoder[A](implicit A: => DerivedRowDecoder[A]): RowDecoder[A]          = A

  implicit def autoDerivedFamilyEncoder[A](implicit A: => DerivedFamilyEncoder[A]): FamilyEncoder[A] = A
}
