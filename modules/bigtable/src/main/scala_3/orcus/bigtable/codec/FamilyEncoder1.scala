package orcus.bigtable.codec

import com.google.protobuf.ByteString

private[codec] trait FamilyEncoder1:
  inline def derived[A](using A: DerivedFamilyEncoder[A]): DerivedFamilyEncoder[A] = A
