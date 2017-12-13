package orcus.codec

import java.{util => ju}

import org.apache.hadoop.hbase.util.Bytes
import org.scalatest.{FunSuite, Matchers}

class FamilyDecoderSpec extends FunSuite with Matchers {

  test("flatMap/map should return it mapped value") {
    final case class Foo(a: Int)
    final case class Bar(b: String)

    val t = new ju.TreeMap[Array[Byte], Array[Byte]](Bytes.BYTES_COMPARATOR)
    t.put(Bytes.toBytes("a"), Bytes.toBytes(10))
    t.put(Bytes.toBytes("b"), Bytes.toBytes("ss"))

    val f = for {
      x <- FamilyDecoder[Foo]
      y <- FamilyDecoder[Bar]
    } yield (x, y)

    val Right(b) = f(t)
    assert(b === ((Foo(10), Bar("ss"))))
  }

  test("mapF should return mapped value") {
    final case class Foo(a: Int)
    val f = FamilyDecoder[Foo].mapF[String](x => Right(x.a.toString))

    val t = new ju.TreeMap[Array[Byte], Array[Byte]](Bytes.BYTES_COMPARATOR)
    t.put(Bytes.toBytes("a"), Bytes.toBytes(10))

    val Right(b) = f(t)
    assert(b === "10")
  }

  test("It should return left when mapF create the Left") {
    final case class Foo(a: Int)
    val e = new Exception(":)")
    val f = FamilyDecoder[Foo].mapF[String](x => Left(e))

    val t = new ju.TreeMap[Array[Byte], Array[Byte]](Bytes.BYTES_COMPARATOR)
    t.put(Bytes.toBytes("a"), Bytes.toBytes(10))

    val l = f(t)
    assert(l === Left(e))
  }

  test("It should return empty map when family is null") {
    val f = FamilyDecoder[Map[String, String]]
    assert(f(null) === Right(Map.empty[String, String]))
  }
}
