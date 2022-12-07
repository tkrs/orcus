package orcus.bigtable.codec

private[codec] trait FamilyDecoder1:
  inline def derived[A](using A: DerivedFamilyDecoder[A]): DerivedFamilyDecoder[A] = A
