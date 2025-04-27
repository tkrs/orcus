package orcus.bigtable.codec

object semiauto {
  def derivedFamilyDecoder[A](using A: => DerivedFamilyDecoder[A]): FamilyDecoder[A] = A
  def derivedRowDecoder[A](using A: => DerivedRowDecoder[A]): RowDecoder[A]          = A

  def derivedFamilyEncoder[A](using A: => DerivedFamilyEncoder[A]): FamilyEncoder[A] = A
}
