package orcus.codec

import java.{util => ju}

import org.apache.hadoop.hbase.client.Result
import org.apache.hadoop.hbase.util.Bytes
import org.scalatest.FunSuite
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito._

class DecoderSpec extends FunSuite with MockitoSugar {

  test("flatMap & map") {
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
}
