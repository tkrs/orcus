package orcus

import java.nio.ByteBuffer
import java.util

import cats.instances.either._
import org.apache.hadoop.hbase.Cell
import org.apache.hadoop.hbase.client.Result
import org.apache.hadoop.hbase.util.Bytes
import org.scalatest._
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers._

import scala.collection.JavaConverters._

class ResultSpec extends FunSpec with MockitoSugar with Matchers {
  import codec.generic.derived._

  type F[A] = Either[Throwable, A]

  describe("getRow") {
    it("should take the row from Result.getRow() as-is") {
      val m = mock[Result]

      val expected = "3"
      val row      = Bytes.toBytes(expected)

      when(m.getRow).thenReturn(row)

      val Right(Some(v)) = result.getRow[F](m)

      assert(expected === Bytes.toString(v))
      verify(m).getRow
    }
    it("should return empty when Result.getRow() returns null") {
      val m = mock[Result]

      when(m.getRow).thenReturn(null)

      val Right(v) = result.getRow[F](m)

      assert(v.isEmpty)
      verify(m).getRow
    }
  }

  describe("rawCells") {
    it("should take the cells from Result.rawCells() and convert it to scala's Seq") {
      val m     = mock[Result]
      val cells = Iterator.continually(mock[Cell]).take(10).toArray[Cell]

      when(m.rawCells()).thenReturn(cells)

      val Right(v) = result.rawCells[F](m)

      assert(v === cells.toSeq)
      verify(m).rawCells()
    }
    it("should return empty when Result.rawCells() returns null") {
      val m     = mock[Result]
      val cells = null

      when(m.rawCells()).thenReturn(cells)

      val Right(v) = result.rawCells[F](m)

      assert(v.isEmpty)
      verify(m).rawCells()
    }
  }

  describe("getColumnCells") {
    it("should take the value from Result.getColumnCells(Array[Byte], Array[Byte]) and convert it to scala's Seq") {
      val m   = mock[Result]
      val cfn = Bytes.toBytes("1")
      val cn  = Bytes.toBytes("2")

      val cells = Iterator.continually(mock[Cell]).take(10).toList.asJava

      when(m.getColumnCells(cfn, cn)).thenReturn(cells)

      val Right(v) = result.getColumnCells[F](m, cfn, cn)

      assert(v === cells.asScala)
      verify(m).getColumnCells(cfn, cn)
    }
  }

  describe("getColumnLatestCell") {
    it("should take the value from Result.getColumnLatestCell(Array[Byte], Array[Byte])") {
      val m   = mock[Result]
      val cfn = Bytes.toBytes("1")
      val cn  = Bytes.toBytes("2")

      val cell = mock[Cell]

      when(m.getColumnLatestCell(cfn, cn)).thenReturn(cell)

      val Right(Some(v)) = result.getColumnLatestCell[F](m, cfn, cn)

      assert(v === cell)
      verify(m).getColumnLatestCell(cfn, cn)
    }
    it("should return empty when Result.getColumnLatestCell(Array[Byte], Array[Byte]) returns null") {
      val m   = mock[Result]
      val cfn = Bytes.toBytes("1")
      val cn  = Bytes.toBytes("2")

      when(m.getColumnLatestCell(cfn, cn)).thenReturn(null)

      val Right(v) = result.getColumnLatestCell[F](m, cfn, cn)

      assert(v.isEmpty)
      verify(m).getColumnLatestCell(cfn, cn)
    }
  }

  describe("get") {
    it("should take the value from Result.getValue(Array[Byte], Array[Byte]) and convert it to arbitrary types") {
      val m   = mock[Result]
      val cfn = Bytes.toBytes("1")
      val cn  = Bytes.toBytes("2")

      val expected = Double.MaxValue
      val value    = Bytes.toBytes(expected)

      when(m.getValue(cfn, cn)).thenReturn(value)

      val Right(Some(v)) = result.get[Double, F](m, cfn, cn)

      assert(v === expected)
    }
  }

  describe("getValue") {
    it("should take the value from Result.getValue(Array[Byte], Array[Byte]) as-is") {
      val m   = mock[Result]
      val cfn = Bytes.toBytes("1")
      val cn  = Bytes.toBytes("2")

      val expected = "3"
      val value    = Bytes.toBytes(expected)

      when(m.getValue(cfn, cn)).thenReturn(value)

      val Right(Some(v)) = result.getValue[F](m, cfn, cn)

      assert(Bytes.toString(v) === expected)
      verify(m).getValue(cfn, cn)
    }
    it("should return empty when Result.getValue(Array[Byte], Array[Byte]) returns null") {
      val m   = mock[Result]
      val cfn = Bytes.toBytes("1")
      val cn  = Bytes.toBytes("2")

      when(m.getValue(cfn, cn)).thenReturn(null)

      val Right(v) = result.getValue[F](m, cfn, cn)

      assert(v.isEmpty)
      verify(m).getValue(cfn, cn)
    }
  }

  describe("getValueAsByteBuffer") {
    it("should take the value from Result.getValueAsByteBuffer(Array[Byte], Array[Byte])") {
      val m   = mock[Result]
      val cfn = Bytes.toBytes("1")
      val cn  = Bytes.toBytes("2")

      val expected = "3"
      val value    = ByteBuffer.wrap(Bytes.toBytes(expected))

      when(m.getValueAsByteBuffer(cfn, cn)).thenReturn(value)

      val Right(Some(v)) = result.getValueAsByteBuffer[F](m, cfn, cn)

      assert(Bytes.toString(v.array()) === expected)
      verify(m).getValueAsByteBuffer(cfn, cn)
    }
    it("should return empty when Result.getValueAsByteBuffer(Array[Byte], Array[Byte]) return null") {
      val m   = mock[Result]
      val cfn = Bytes.toBytes("1")
      val cn  = Bytes.toBytes("2")

      when(m.getValueAsByteBuffer(cfn, cn)).thenReturn(null)

      val Right(e) = result.getValueAsByteBuffer[F](m, cfn, cn)

      assert(e.isEmpty)
      verify(m).getValueAsByteBuffer(cfn, cn)
    }
  }

  describe("getFamilyMap") {
    it("should take the map from Result.getFamilyMap(Array[Byte]) and convert it to scala's Map") {
      val m   = mock[Result]
      val cfn = Bytes.toBytes("1")

      val map = mock[util.NavigableMap[Array[Byte], Array[Byte]]]

      when(m.getFamilyMap(cfn)).thenReturn(map)

      val Right(v) = result.getFamilyMap[F](m, cfn)

      assert(v === map.asScala.toMap)
      verify(m).getFamilyMap(cfn)
    }
    it("should return empty when Result.getFamilyMap(Array[Byte]) returns null") {
      val m   = mock[Result]
      val cfn = Bytes.toBytes("1")

      when(m.getFamilyMap(cfn)).thenReturn(null)

      val Right(v) = result.getFamilyMap[F](m, cfn)

      assert(v.isEmpty)
      verify(m).getFamilyMap(cfn)
    }
  }

  describe("getFamily") {
    it("should convert to arbitrary type obtained from getFamilyMap") {
      final case class Foo(x: Int, y: String, z: Boolean)

      val m   = mock[Result]
      val cfn = Bytes.toBytes("1")

      val map = new util.TreeMap[Array[Byte], Array[Byte]](Bytes.BYTES_COMPARATOR)
      val foo = Foo(x = Int.MinValue, y = "Johann Carl Friedrich Gauss", z = true)

      map.put(Bytes.toBytes("x"), Bytes.toBytes(foo.x))
      map.put(Bytes.toBytes("y"), Bytes.toBytes(foo.y))
      map.put(Bytes.toBytes("z"), Bytes.toBytes(foo.z))

      when(m.getFamilyMap(cfn)).thenReturn(map)

      val Right(v) = result.getFamily[Option[Foo], F](m, cfn)

      assert(v === Some(foo))
    }
    it("should convert to typed Map obtained from getFamilyMap") {

      val m   = mock[Result]
      val cfn = Bytes.toBytes("1")

      val map = new util.TreeMap[Array[Byte], Array[Byte]](Bytes.BYTES_COMPARATOR)
      val foo = Map(20 -> "x", 30 -> "y", 40 -> "z")

      map.put(Bytes.toBytes(20), Bytes.toBytes("x"))
      map.put(Bytes.toBytes(30), Bytes.toBytes("y"))
      map.put(Bytes.toBytes(40), Bytes.toBytes("z"))

      when(m.getFamilyMap(cfn)).thenReturn(map)

      val Right(v) = result.getFamily[Map[Int, String], F](m, cfn)

      assert(v === foo)
    }
    it("should return empty when getFamilyMap returns null") {
      final case class Foo(x: Int, y: String, z: Boolean)

      val m   = mock[Result]
      val cfn = Bytes.toBytes("1")

      when(m.getFamilyMap(cfn)).thenReturn(null)

      val Right(v) = result.getFamily[Option[Foo], F](m, cfn)

      assert(v.isEmpty)
    }
  }

  describe("to") {
    it("should convert to arbitrary type") {
      final case class Bar(x: Int, y: String, z: Boolean)
      final case class Quux(a: Int)
      final case class Foo(bar: Bar, quux: Option[Quux])

      val m = mock[Result]

      val map  = new util.TreeMap[Array[Byte], Array[Byte]](Bytes.BYTES_COMPARATOR)
      val quux = Quux(a = -1)
      val bar  = Bar(x = Int.MinValue, y = "Johann Carl Friedrich Gauss", z = true)

      map.put(Bytes.toBytes("x"), Bytes.toBytes(bar.x))
      map.put(Bytes.toBytes("y"), Bytes.toBytes(bar.y))
      map.put(Bytes.toBytes("z"), Bytes.toBytes(bar.z))
      map.put(Bytes.toBytes("a"), Bytes.toBytes(quux.a))

      when(m.getFamilyMap(any[Array[Byte]])).thenReturn(map)

      val Right(v) = result.to[Option[Foo], F](m)

      assert(v === Some(Foo(bar, Some(quux))))
    }
  }
}
