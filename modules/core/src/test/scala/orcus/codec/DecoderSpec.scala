package orcus.codec

import java.util

import cats.Eval
import orcus.internal.Utils
import org.apache.hadoop.hbase.client.Result
import org.apache.hadoop.hbase.util.Bytes
import org.scalatest.flatspec.AnyFlatSpec

class DecoderSpec extends AnyFlatSpec with CodecSpec {
  "decodeMapLike" should "decode the nested map" in {
    val cells = Utils.toJavaList(
      Seq(
        cell("row", "cf1", "a", Bytes.toBytes("a")),
        cell("row", "cf1", "b", Bytes.toBytes("")),
        cell("row", "cf1", "c", null)
      )
    )

    val f = Decoder[Map[String, Map[String, String]]]
    val expected = Right(Map("cf1" -> Map("a" -> "a", "b" -> "", "c" -> "")))

    assert(f(Result.create(cells)) === expected)
  }

  it should "return empty map when cells is empty" in {
    val f = Decoder[Map[String, Map[String, String]]]
    assert(f(Result.EMPTY_RESULT) === Right(Map.empty))
  }

  it should "fail decode when a value decode is failed" in {
    val expected = new Exception
    class E
    implicit val decodeE: FamilyDecoder[E] =
      (_: util.NavigableMap[Array[Byte], Array[Byte]]) => Left(expected)
    val cells = Utils.toJavaList(Seq(cell("row", "cf1", "a", Bytes.toBytes("a"))))
    val f = Decoder[Map[String, E]]

    assert(f(Result.create(cells)) === Left(expected))
  }

  "decodeOption" should "fail decode when the value decoder is failed" in {
    val expected = new Exception
    class E
    implicit val decodeE: Decoder[E] =
      (_: Result) => Left(expected)
    val cells = Utils.toJavaList(Seq(cell("row", "cf1", "a", Bytes.toBytes("a"))))
    val f = Decoder[Option[E]]

    assert(f(Result.create(cells)) === Left(expected))
  }

  "pure" should "create decoder from pure value" in {
    val f = Decoder.pure(10)
    assert(f(Result.EMPTY_RESULT) === Right(10))
  }

  "eval" should "create decoder from Eval" in {
    val f = Decoder.eval(Eval.now(10))
    assert(f(Result.EMPTY_RESULT) === Right(10))
  }

  "liftF" should "create decoder from Either" in {
    val f = Decoder.liftF(Right(10))
    assert(f(Result.EMPTY_RESULT) === Right(10))
  }

  "flatMap/map" should "return mapped value" in {
    val f = for {
      x <- Decoder.pure(9)
      y <- Decoder.pure(1)
    } yield x + y

    assert(f(Result.EMPTY_RESULT) === Right(10))
  }

  it should "return Left when its upstream decoder was failed" in {
    val expected = Left[Throwable, Int](new Exception)
    val f = Decoder.liftF(expected).flatMap[Int](_ => fail()).map[Int](_ => fail())

    assert(f(Result.EMPTY_RESULT) === expected)
  }

  "mapF" should "return mapped value" in {
    val f = Decoder.pure(1).mapF(a => Right(a))
    assert(f(Result.EMPTY_RESULT) === Right(1))
  }
  it should "return Left when its upstream decoder was failed" in {
    val expected = new Exception
    val f = Decoder.liftF[Int](Left(expected)).mapF(_ => fail())
    assert(f(Result.EMPTY_RESULT) === Left(expected))
  }
}
