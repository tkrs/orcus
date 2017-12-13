package orcus.codec

import java.{util => ju}

import org.apache.hadoop.hbase.util.Bytes
import org.scalatest.FunSuite

class FamilyDecoderSpec extends FunSuite {

  test("flatMap & map") {
    final case class Foo(a: Int)
    final case class Bar(b: String)

    val t = new ju.TreeMap[Array[Byte], Array[Byte]](Bytes.BYTES_COMPARATOR)
    t.put(Bytes.toBytes("a"), Bytes.toBytes(10))
    t.put(Bytes.toBytes("b"), Bytes.toBytes("ss"))

    val f = for {
      x <- FamilyDecoder[Foo]
      y <- FamilyDecoder[Bar]
    } yield y

    val Right(b) = f(t)
    assert(b === Bar("ss"))
  }

  test("It should return empty map when family is null") {
    val f = FamilyDecoder[Map[String, String]]
    assert(f(null) === Right(Map.empty[String, String]))
  }
}
