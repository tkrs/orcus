package orcus.bigtable.codec

import com.google.common.primitives.Ints
import com.google.common.primitives.Longs
import com.google.common.primitives.Shorts
import com.google.protobuf.ByteString
import org.scalatest.funsuite.AnyFunSuite

class PrimitiveEncoderSpec extends AnyFunSuite {

  test("PrimitiveEncoder[String]") {
    val s = "foobarμ"
    val b = PrimitiveEncoder[String].apply(s)
    assert(b.toStringUtf8 === s)
  }
  test("PrimitiveEncoder[ByteString]") {
    val s = ByteString.copyFromUtf8("f80athewarare")
    val b = PrimitiveEncoder[ByteString].apply(s)
    assert(s === b)
  }
  test("PrimitiveEncoder[Array[Byte]]") {
    val s = Array(1, 2, 3, 4).map(_.toByte)
    val b = PrimitiveEncoder[Array[Byte]].apply(s)
    assert(b.toByteArray.toSeq === s.toSeq)
  }
  test("PrimitiveEncoder[Long]") {
    locally {
      val b = PrimitiveEncoder[Long].apply(Long.MaxValue)
      assert(Longs.fromByteArray(b.toByteArray) === Long.MaxValue)
    }
    locally {
      val b = PrimitiveEncoder[Long].apply(Long.MinValue)
      assert(Longs.fromByteArray(b.toByteArray) === Long.MinValue)
    }
  }
  test("PrimitiveEncoder[Boolean]") {
    locally {
      val b = PrimitiveEncoder[Boolean].apply(true)
      assert(b.toByteArray.apply(0) === -1.toByte)
    }
    locally {
      val b = PrimitiveEncoder[Boolean].apply(false)
      assert(b.toByteArray.apply(0) === 0.toByte)
    }
  }
  test("PrimitiveEncoder[Short]") {
    locally {
      val b = PrimitiveEncoder[Short].apply(Short.MaxValue)
      assert(Shorts.fromByteArray(b.toByteArray) === Short.MaxValue)
    }
    locally {
      val b = PrimitiveEncoder[Short].apply(Short.MinValue)
      assert(Shorts.fromByteArray(b.toByteArray) === Short.MinValue)
    }
  }
  test("PrimitiveEncoder[Int]") {
    locally {
      val b = PrimitiveEncoder[Int].apply(Int.MaxValue)
      assert(Ints.fromByteArray(b.toByteArray) === Int.MaxValue)
    }
    locally {
      val b = PrimitiveEncoder[Int].apply(Int.MinValue)
      assert(Ints.fromByteArray(b.toByteArray) === Int.MinValue)
    }
  }
  test("PrimitiveEncoder[Float]") {
    locally {
      val b = PrimitiveEncoder[Float].apply(Float.MaxValue)
      assert(java.lang.Float.intBitsToFloat(Ints.fromByteArray(b.toByteArray)) === Float.MaxValue)
    }
    locally {
      val b = PrimitiveEncoder[Float].apply(Float.MinValue)
      assert(java.lang.Float.intBitsToFloat(Ints.fromByteArray(b.toByteArray)) === Float.MinValue)
    }
  }
  test("PrimitiveEncoder[Double]") {
    locally {
      val b = PrimitiveEncoder[Double].apply(Double.MaxValue)
      assert(java.lang.Double.longBitsToDouble(Longs.fromByteArray(b.toByteArray)) === Double.MaxValue)
    }
    locally {
      val b = PrimitiveEncoder[Double].apply(Double.MinValue)
      assert(java.lang.Double.longBitsToDouble(Longs.fromByteArray(b.toByteArray)) === Double.MinValue)
    }
  }
  test("PrimitiveEncoder[Option[A]]") {
    locally {
      val b = PrimitiveEncoder[Option[Int]].apply(Option(10))
      assert(Ints.fromByteArray(b.toByteArray) === 10)
    }
    locally {
      val b = PrimitiveEncoder[Option[Int]].apply(Option.empty)
      assert(b.isEmpty)
    }
  }
}
