package orcus.codec

import java.{util => ju}

import cats.Eval
import cats.syntax.option._
import org.apache.hadoop.hbase.KeyValue.Type
import org.apache.hadoop.hbase.{Cell, CellUtil}
import org.apache.hadoop.hbase.client.Result
import org.apache.hadoop.hbase.util.Bytes
import org.scalatest.FunSuite
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito._

import scala.collection.JavaConverters._

class DecoderSpec extends FunSuite with MockitoSugar {

  test("flatMap/map should return it mapped value") {
    final case class Quux(b: String)
    final case class Wuu(a: Int)
    final case class Foo(b: Quux)
    final case class Bar(b: Wuu)

    val m = mock[Result]

    val t = new ju.TreeMap[Array[Byte], Array[Byte]](Bytes.BYTES_COMPARATOR)
    t.put(Bytes.toBytes("a"), Bytes.toBytes(10))
    t.put(Bytes.toBytes("b"), Bytes.toBytes("ss"))

    when(m.getFamilyMap(Bytes.toBytes("b"))).thenReturn(t)

    val f = for {
      x <- Decoder[Foo]
      y <- Decoder[Bar]
    } yield y

    val Right(b) = f(m)
    assert(b === Bar(Wuu(10)))
  }

  test("mapF should return it mapped value") {
    final case class Quux(b: String)
    final case class Wuu(a: Int)
    final case class Foo(b: Quux)
    final case class Bar(b: Wuu)

    val m = mock[Result]

    val t = new ju.TreeMap[Array[Byte], Array[Byte]](Bytes.BYTES_COMPARATOR)
    t.put(Bytes.toBytes("a"), Bytes.toBytes(10))
    t.put(Bytes.toBytes("b"), Bytes.toBytes("ss"))

    when(m.getFamilyMap(Bytes.toBytes("b"))).thenReturn(t)

    val f        = Decoder[Foo].mapF(x => Right(x.b))
    val Right(b) = f(m)
    assert(b === Quux("ss"))
  }

  test("mapF should return error when it mapped to Left") {
    final case class Quux(b: String)
    final case class Wuu(a: Int)
    final case class Foo(b: Quux)
    final case class Bar(b: Wuu)

    val m = mock[Result]

    val t = new ju.TreeMap[Array[Byte], Array[Byte]](Bytes.BYTES_COMPARATOR)
    t.put(Bytes.toBytes("a"), Bytes.toBytes(10))
    t.put(Bytes.toBytes("b"), Bytes.toBytes("ss"))

    when(m.getFamilyMap(Bytes.toBytes("b"))).thenReturn(t)

    val ex = new Exception(";)")
    val f  = Decoder[Foo].mapF(x => Left(ex))
    val l  = f(m)
    assert(l === Left(ex))
  }

  test("pure") {
    val m = mock[Result]
    val f = Decoder.pure(10)(m)
    assert(f === Right(10))
  }

  test("eval") {
    val m = mock[Result]
    val f = Decoder.eval(Eval.now(10))(m)
    assert(f === Right(10))
  }

  test("liftF") {
    val m = mock[Result]
    val f = Decoder.liftF(Right(10))(m)
    assert(f === Right(10))
  }

  test("It should derive the map") {
    case class All(a: Option[Int] = None,
                   b: Option[Float] = None,
                   c: Option[Long] = None,
                   d: Option[Double] = None,
                   e: Option[String] = None,
                   g: Option[Boolean] = None,
                   h: Option[Short] = None,
                   i: Option[BigDecimal] = None)
    val f   = Decoder[Map[String, All]]
    val row = Bytes.toBytes("row")
    val cf1 = Bytes.toBytes("cf1")

    def cell(q: String, v: Array[Byte]): Cell =
      CellUtil.createCell(row, cf1, Bytes.toBytes(q), Long.MaxValue, Type.Put, v, null)

    val cells = Seq(
      cell("a", Bytes.toBytes(1)),
      cell("b", Bytes.toBytes(1.1f)),
      cell("c", Bytes.toBytes(100L)),
      cell("d", Bytes.toBytes(1.9)),
      cell("e", Bytes.toBytes("s")),
      cell("g", Bytes.toBytes(true)),
      cell("h", Bytes.toBytes(Short.MaxValue)),
      cell("i", Bytes.toBytes(BigDecimal(10).bigDecimal))
    ).asJava

    val result = Result.create(cells)
    val x      = result.rawCells().toSeq
    println(x)
    val expected = Right(
      Map(
        "cf1" -> All(1.some,
                     1.1f.some,
                     100L.some,
                     1.9.some,
                     "s".some,
                     true.some,
                     Short.MaxValue.some,
                     BigDecimal(10).some)))

    assert(f(result) === expected)
  }
}
