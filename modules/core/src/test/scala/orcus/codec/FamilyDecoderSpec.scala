package orcus.codec

import java.{util => ju}

import cats.Eval
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

  test("mapF should return left when mapF create the Left") {
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

  test("It should get a null values as empty when its String and column value is absent or empty") {
    val f = FamilyDecoder[Map[String, String]]
    val m = new ju.TreeMap[Array[Byte], Array[Byte]](Bytes.BYTES_COMPARATOR)
    m.put(Bytes.toBytes("a"), null)
    m.put(Bytes.toBytes("b"), Array.emptyByteArray)
    m.put(Bytes.toBytes("c"), Bytes.toBytes("d"))
    assert(f(m) === Right(Map("a" -> "", "b" -> "", "c" -> "d")))
  }

  test("It should avoid a null values when its column value is null/empty") {
    val f = FamilyDecoder[Map[String, Boolean]]
    val m = new ju.TreeMap[Array[Byte], Array[Byte]](Bytes.BYTES_COMPARATOR)
    m.put(Bytes.toBytes("a"), null)
    m.put(Bytes.toBytes("b"), Array.emptyByteArray)
    m.put(Bytes.toBytes("c"), Bytes.toBytes(true))
    m.put(Bytes.toBytes("d"), Bytes.toBytes(false))
    assert(f(m) === Right(Map("c" -> true, "d" -> false)))
  }

  test("It should return map with empty values when column value is empty") {
    case class All(a: Option[Int] = None,
                   b: Option[Float] = None,
                   c: Option[Long] = None,
                   d: Option[Double] = None,
                   e: Option[String] = None,
                   f: Option[Array[Byte]] = None,
                   g: Option[Boolean] = None,
                   h: Option[Short] = None,
                   i: Option[BigDecimal] = None)
    val f = FamilyDecoder[All]
    val m = new ju.TreeMap[Array[Byte], Array[Byte]](Bytes.BYTES_COMPARATOR)
    m.put(Bytes.toBytes("a"), Array.emptyByteArray)
    m.put(Bytes.toBytes("b"), Array.emptyByteArray)
    m.put(Bytes.toBytes("c"), Array.emptyByteArray)
    m.put(Bytes.toBytes("d"), Array.emptyByteArray)
    m.put(Bytes.toBytes("e"), Array.emptyByteArray)
    m.put(Bytes.toBytes("f"), Array.emptyByteArray)
    m.put(Bytes.toBytes("g"), Array.emptyByteArray)
    m.put(Bytes.toBytes("h"), Array.emptyByteArray)
    m.put(Bytes.toBytes("i"), Array.emptyByteArray)
    assert(f(m) === Right(All()))
  }

  test("pure") {
    val m = new ju.TreeMap[Array[Byte], Array[Byte]]()
    val f = FamilyDecoder.pure(10)(m)
    assert(f === Right(10))
  }

  test("eval") {
    val m = new ju.TreeMap[Array[Byte], Array[Byte]]()
    val f = FamilyDecoder.eval(Eval.now(10))(m)
    assert(f === Right(10))
  }

  test("liftF") {
    val m = new ju.TreeMap[Array[Byte], Array[Byte]]()
    val f = FamilyDecoder.liftF(Right(10))(m)
    assert(f === Right(10))
  }
}
