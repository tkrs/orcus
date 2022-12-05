package orcus.bigtable.codec

object semiauto {
  def derivedFamilyDecoder[A](implicit A: => DerivedFamilyDecoder[A]): FamilyDecoder[A] = A
  def derivedRowDecoder[A](implicit A: => DerivedRowDecoder[A]): RowDecoder[A]          = A

  def derivedFamilyEncoder[A](implicit A: => DerivedFamilyEncoder[A]): FamilyEncoder[A] = A
}
