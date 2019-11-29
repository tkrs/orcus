package orcus.codec.generic

import java.{util => ju}

import orcus.codec.FamilyDecoder
import orcus.codec.semiauto.derivedFamilyDecoder
import org.apache.hadoop.hbase.util.Bytes
import org.scalatest.FlatSpec

class DerivedFamilyDecoderSpec extends FlatSpec {
  case class Foo(a: Int)

  object Foo {
    implicit val decodeFamilyFoo: FamilyDecoder[Foo] = derivedFamilyDecoder[Foo]
  }

  it should "decode a case class" in {
    val t = new ju.TreeMap[Array[Byte], Array[Byte]](Bytes.BYTES_COMPARATOR)
    t.put(Bytes.toBytes("a"), Bytes.toBytes(10))
    val Right(x) = FamilyDecoder[Foo].apply(t)

    assert(x === Foo(10))
  }

  it should "fail decode when the require property is absent" in {
    val t = new ju.TreeMap[Array[Byte], Array[Byte]](Bytes.BYTES_COMPARATOR)
    val x = FamilyDecoder[Foo].apply(t)

    assert(x.isLeft)
  }
}
