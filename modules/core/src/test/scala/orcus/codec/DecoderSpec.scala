package orcus.codec

import java.{util => ju}

import cats.Eval
import cats.syntax.option._
import org.apache.hadoop.hbase.{Cell, CellBuilderType, ExtendedCellBuilderFactory}
import org.apache.hadoop.hbase.client.Result
import org.apache.hadoop.hbase.util.Bytes
import org.scalatest.FunSpec
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito._

import scala.collection.JavaConverters._

class DecoderSpec extends FunSpec with MockitoSugar {

  describe("flatMap/map") {
    it("should return mapped value") {
      case class Quux(b: String)
      case class Wuu(a: Int)
      case class Foo(b: Quux)
      case class Bar(b: Wuu)

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
    describe("flatMap") {
      it("should return Left when its decoding is failed") {
        case class Quux(b: Int)
        case class Foo(b: Quux)

        val m = mock[Result]

        val t = new ju.TreeMap[Array[Byte], Array[Byte]](Bytes.BYTES_COMPARATOR)
        t.put(Bytes.toBytes("b"), Bytes.toBytes("ss"))

        when(m.getFamilyMap(Bytes.toBytes("b"))).thenReturn(t)

        val f = Decoder[Foo].flatMap(_ => fail())

        val b = f(m)
        assert(b.isLeft)
      }
    }
    describe("map") {
      it("should return Left when its decoding is failed") {
        case class Quux(b: Int)
        case class Foo(b: Quux)

        val m = mock[Result]

        val t = new ju.TreeMap[Array[Byte], Array[Byte]](Bytes.BYTES_COMPARATOR)
        t.put(Bytes.toBytes("b"), Bytes.toBytes("100"))

        when(m.getFamilyMap(Bytes.toBytes("b"))).thenReturn(t)

        val f = Decoder[Foo].map(a => fail())

        val b = f(m)
        assert(b.isLeft)
      }
    }
  }

  describe("mapF") {
    it("should return mapped value") {
      case class Quux(b: String)
      case class Wuu(a: Int)
      case class Foo(b: Quux)
      case class Bar(b: Wuu)

      val m = mock[Result]

      val t = new ju.TreeMap[Array[Byte], Array[Byte]](Bytes.BYTES_COMPARATOR)
      t.put(Bytes.toBytes("a"), Bytes.toBytes(10))
      t.put(Bytes.toBytes("b"), Bytes.toBytes("ss"))

      when(m.getFamilyMap(Bytes.toBytes("b"))).thenReturn(t)

      val f        = Decoder[Foo].mapF(x => Right(x.b))
      val Right(b) = f(m)
      assert(b === Quux("ss"))
    }
    it("should return error when its mapped to Left") {
      case class Quux(b: String)
      case class Foo(b: Quux)

      val m = mock[Result]

      val t = new ju.TreeMap[Array[Byte], Array[Byte]](Bytes.BYTES_COMPARATOR)
      t.put(Bytes.toBytes("a"), Bytes.toBytes(10))
      t.put(Bytes.toBytes("b"), Bytes.toBytes("ss"))

      when(m.getFamilyMap(Bytes.toBytes("b"))).thenReturn(t)

      val ex = new Exception(";)")
      val f  = Decoder[Foo].mapF(x => Left(ex))
      val l  = f(m)
      assert(l.isLeft)
    }
    it("should return error when its decoding is failed") {
      case class Quux(b: Int)
      case class Foo(b: Quux)

      val m = mock[Result]

      val t = new ju.TreeMap[Array[Byte], Array[Byte]](Bytes.BYTES_COMPARATOR)
      t.put(Bytes.toBytes("a"), Bytes.toBytes(10))
      t.put(Bytes.toBytes("b"), Bytes.toBytes("ss"))

      when(m.getFamilyMap(Bytes.toBytes("b"))).thenReturn(t)

      val f = Decoder[Foo].mapF(x => fail())
      val l = f(m)
      assert(l.isLeft)
    }
  }

  describe("pure") {
    it("should create for pure") {
      val m = mock[Result]
      val f = Decoder.pure(10)(m)
      assert(f === Right(10))
    }
  }

  describe("eval") {
    it("should create from Eval") {
      val m = mock[Result]
      val f = Decoder.eval(Eval.now(10))(m)
      assert(f === Right(10))
    }
  }

  describe("liftF") {
    it("should create liftF") {
      val m = mock[Result]
      val f = Decoder.liftF(Right(10))(m)
      assert(f === Right(10))
    }
  }

  describe("Decoding") {
    it("should derive the map") {
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

      def cell(q: String, v: Array[Byte]): Cell = {
        val builder = ExtendedCellBuilderFactory.create(CellBuilderType.DEEP_COPY)
        builder
          .setRow(row)
          .setFamily(cf1)
          .setQualifier(Bytes.toBytes(q))
          .setTimestamp(Long.MaxValue)
          .setType(Cell.Type.Put)
          .setValue(v)
          .build()
      }

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

    it("should derive the nested map") {
      val f   = Decoder[Map[String, Map[String, String]]]
      val row = Bytes.toBytes("row")
      val cf1 = Bytes.toBytes("cf1")

      def cell(q: String, v: Array[Byte]): Cell = {
        val builder = ExtendedCellBuilderFactory.create(CellBuilderType.DEEP_COPY)
        builder
          .setRow(row)
          .setFamily(cf1)
          .setQualifier(Bytes.toBytes(q))
          .setTimestamp(Long.MaxValue)
          .setType(Cell.Type.Put)
          .setValue(v)
          .build()
      }

      val cells = Seq(
        cell("a", Bytes.toBytes("a")),
        cell("b", Bytes.toBytes("")),
        cell("c", null)
      ).asJava

      val result   = Result.create(cells)
      val expected = Right(Map("cf1" -> Map("a" -> "a", "b" -> "", "c" -> "")))

      assert(f(result) === expected)
    }

    it("should return empty map when cells is empty") {
      val f      = Decoder[Map[String, Map[String, String]]]
      val cells  = new ju.ArrayList[Cell]()
      val result = Result.create(cells)
      assert(f(result) === Right(Map.empty))
    }
  }
}
