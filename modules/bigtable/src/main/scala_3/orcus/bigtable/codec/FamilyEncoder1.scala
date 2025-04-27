package orcus.bigtable.codec

private[codec] trait FamilyEncoder1:
  inline def derived[A](using A: DerivedFamilyEncoder[A]): DerivedFamilyEncoder[A] = A
