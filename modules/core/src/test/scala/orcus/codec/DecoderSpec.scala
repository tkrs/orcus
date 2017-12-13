package orcus.codec

import java.{util => ju}

import org.apache.hadoop.hbase.client.Result
import org.apache.hadoop.hbase.util.Bytes
import org.scalatest.FunSuite
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito._

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
}
