package orcus.bigtable.codec

import com.google.protobuf.ByteString
import shapeless3.deriving._

trait DerivedFamilyEncoder[A] extends FamilyEncoder[A]

object DerivedFamilyEncoder extends DerivedFamilyEncoder1

private[codec] trait DerivedFamilyEncoder1:

  given familyEncoderGen[A](using
    inst: K0.ProductInstances[PrimitiveEncoder, A],
    labelling: Labelling[A]
  ): DerivedFamilyEncoder[A] with
    def apply(v: A): Map[ByteString, ByteString] =
      val builder = Map.newBuilder[ByteString, ByteString]
      labelling.elemLabels.zipWithIndex.foreach { case (l, i) =>
        builder += ByteString.copyFromUtf8(l) -> inst.project(v)(i)(
          [t] => (f: PrimitiveEncoder[t], pt: t) => f(pt)
        )
      }
      builder.result()

  inline def derived[A](using K0.ProductGeneric[A]): DerivedFamilyEncoder[A] = familyEncoderGen
