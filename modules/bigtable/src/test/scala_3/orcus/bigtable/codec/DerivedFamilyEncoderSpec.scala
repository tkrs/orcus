package orcus.bigtable.codec

import com.google.common.primitives.Longs
import com.google.protobuf.ByteString
import org.scalatest.funsuite.AnyFunSuite

class DerivedFamilyEncoderSpec extends AnyFunSuite {

  case class Foo(id: Long, name: String)

  test("auto") {
    import auto.given

    val f = FamilyEncoder[Foo].apply(Foo(8437L, "tkrs"))

    assert(
      f === Map(
        ByteString.copyFromUtf8("id")   -> ByteString.copyFrom(Longs.toByteArray(8437L)),
        ByteString.copyFromUtf8("name") -> ByteString.copyFromUtf8("tkrs")
      )
    )
  }

  test("semiauto") {
    val f = semiauto.derivedFamilyEncoder[Foo].apply(Foo(8437L, "tkrs"))

    assert(
      f === Map(
        ByteString.copyFromUtf8("id")   -> ByteString.copyFrom(Longs.toByteArray(8437L)),
        ByteString.copyFromUtf8("name") -> ByteString.copyFromUtf8("tkrs")
      )
    )
  }
}
