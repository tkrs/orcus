package orcus.codec

import java.{util => ju}

import cats.Eval
import org.apache.hadoop.hbase.util.Bytes
import org.scalatest.FunSuite

class FamilyDecoderSpec extends FunSuite {

  test("pure") {
    val f = FamilyDecoder.pure(10)
    assert(f(new ju.TreeMap) === Right(10))
  }

  test("eval") {
    val f = FamilyDecoder.eval(Eval.now(10))
    assert(f(new ju.TreeMap) === Right(10))
  }

  test("liftF") {
    val f = FamilyDecoder.liftF(Right(10))
    assert(f(new ju.TreeMap) === Right(10))
  }

  test("flatMap/map should return it mapped value") {
    val f = for {
      x <- FamilyDecoder.pure(1)
      y <- FamilyDecoder.pure(2)
    } yield x + y

    assert(f(new ju.TreeMap) === Right(3))
  }
  test("flatmap/map should return Left when its upstream decoder was failed") {
    val expected = Left[Throwable, Int](new Exception)
    val f        = FamilyDecoder.liftF(expected).flatMap[Int](_ => fail()).map[Int](_ => fail())

    assert(f(new ju.TreeMap) === expected)
  }

  test("mapF should return mapped value") {
    val f = FamilyDecoder.pure(10).mapF[String](x => Right(x.toString))

    assert(f(new ju.TreeMap) === Right("10"))
  }

  test("mapF should return left when mapF create the Left") {
    val e = new Exception(":)")
    val f = FamilyDecoder.liftF[Int](Left(e)).mapF[String](_ => fail())

    assert(f(new ju.TreeMap) === Left(e))
  }

  test("decodeOption should return Left when the value decoder was failed") {
    val expected = new Exception
    class E
    implicit val decode: FamilyDecoder[E] = FamilyDecoder.liftF(Left(expected))

    val m = new ju.TreeMap[Array[Byte], Array[Byte]](Bytes.BYTES_COMPARATOR)
    m.put(Bytes.toBytes("1"), Bytes.toBytes("1"))
    val f = FamilyDecoder[Option[E]]

    assert(f(m) === Left(expected))
  }

  test("decodeMapLike should return empty map when family is null") {
    val f = FamilyDecoder[Map[String, String]]
    assert(f(null) === Right(Map.empty[String, String]))
  }

  test("decodeMapLike should get a null values as empty when its String and column value is absent or empty") {
    val f = FamilyDecoder[Map[String, String]]
    val m = new ju.TreeMap[Array[Byte], Array[Byte]](Bytes.BYTES_COMPARATOR)
    m.put(Bytes.toBytes("a"), null)
    m.put(Bytes.toBytes("b"), Array.emptyByteArray)
    m.put(Bytes.toBytes("c"), Bytes.toBytes("d"))
    assert(f(m) === Right(Map("b" -> "", "c" -> "d")))
  }

  test("decodeMapLike should avoid a null values when its column value is null/empty") {
    val f = FamilyDecoder[Map[String, Boolean]]
    val m = new ju.TreeMap[Array[Byte], Array[Byte]](Bytes.BYTES_COMPARATOR) {
      put(Bytes.toBytes("a"), null)
      put(Bytes.toBytes("b"), Array.emptyByteArray)
      put(Bytes.toBytes("c"), Bytes.toBytes(true))
      put(Bytes.toBytes("d"), Bytes.toBytes(false))
    }
    assert(f(m) === Right(Map("c" -> true, "d" -> false)))
  }
}
