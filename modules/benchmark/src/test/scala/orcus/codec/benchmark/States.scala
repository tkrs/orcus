package orcus.codec.benchmark

import java.{util => ju}

import orcus.codec.auto._
import orcus.codec.{Decoder, PutEncoder}
import orcus.internal.Utils
import org.apache.hadoop.hbase.client.{Put, Result}
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.{Cell, CellBuilderType, ExtendedCellBuilderFactory}
import org.openjdk.jmh.annotations._

object States {
  final case class Table[+A](
    cf1: A
  )

  trait Columns

  final case class Columns10(
    a01: Int = 1,
    a02: Int = 2,
    a03: Int = 3,
    a04: Int = 4,
    a05: Int = 5,
    a06: Int = 6,
    a07: Int = 7,
    a08: Int = 8,
    a09: Int = 9,
    a10: Int = 10
  ) extends Columns

  object Columns10 {
    def fromResult(cf: Array[Byte], r: Result): Columns10 =
      Columns10(
        Bytes.toInt(r.getValue(cf, Bytes.toBytes("a01"))),
        Bytes.toInt(r.getValue(cf, Bytes.toBytes("a02"))),
        Bytes.toInt(r.getValue(cf, Bytes.toBytes("a03"))),
        Bytes.toInt(r.getValue(cf, Bytes.toBytes("a04"))),
        Bytes.toInt(r.getValue(cf, Bytes.toBytes("a05"))),
        Bytes.toInt(r.getValue(cf, Bytes.toBytes("a06"))),
        Bytes.toInt(r.getValue(cf, Bytes.toBytes("a07"))),
        Bytes.toInt(r.getValue(cf, Bytes.toBytes("a08"))),
        Bytes.toInt(r.getValue(cf, Bytes.toBytes("a09"))),
        Bytes.toInt(r.getValue(cf, Bytes.toBytes("a10")))
      )
  }

  final case class Columns30(
    a01: Int = 1,
    a02: Int = 2,
    a03: Int = 3,
    a04: Int = 4,
    a05: Int = 5,
    a06: Int = 6,
    a07: Int = 7,
    a08: Int = 8,
    a09: Int = 9,
    a10: Int = 10,
    a11: Int = 11,
    a12: Int = 12,
    a13: Int = 13,
    a14: Int = 14,
    a15: Int = 15,
    a16: Int = 16,
    a17: Int = 17,
    a18: Int = 18,
    a19: Int = 19,
    a20: Int = 20,
    a21: Int = 21,
    a22: Int = 22,
    a23: Int = 23,
    a24: Int = 24,
    a25: Int = 25,
    a26: Int = 26,
    a27: Int = 27,
    a28: Int = 28,
    a29: Int = 29,
    a30: Int = 30
  ) extends Columns

  object Columns30 {
    def fromResult(cf: Array[Byte], r: Result): Columns30 =
      Columns30(
        Bytes.toInt(r.getValue(cf, Bytes.toBytes("a01"))),
        Bytes.toInt(r.getValue(cf, Bytes.toBytes("a02"))),
        Bytes.toInt(r.getValue(cf, Bytes.toBytes("a03"))),
        Bytes.toInt(r.getValue(cf, Bytes.toBytes("a04"))),
        Bytes.toInt(r.getValue(cf, Bytes.toBytes("a05"))),
        Bytes.toInt(r.getValue(cf, Bytes.toBytes("a06"))),
        Bytes.toInt(r.getValue(cf, Bytes.toBytes("a07"))),
        Bytes.toInt(r.getValue(cf, Bytes.toBytes("a08"))),
        Bytes.toInt(r.getValue(cf, Bytes.toBytes("a09"))),
        Bytes.toInt(r.getValue(cf, Bytes.toBytes("a10"))),
        Bytes.toInt(r.getValue(cf, Bytes.toBytes("a11"))),
        Bytes.toInt(r.getValue(cf, Bytes.toBytes("a12"))),
        Bytes.toInt(r.getValue(cf, Bytes.toBytes("a13"))),
        Bytes.toInt(r.getValue(cf, Bytes.toBytes("a14"))),
        Bytes.toInt(r.getValue(cf, Bytes.toBytes("a15"))),
        Bytes.toInt(r.getValue(cf, Bytes.toBytes("a16"))),
        Bytes.toInt(r.getValue(cf, Bytes.toBytes("a17"))),
        Bytes.toInt(r.getValue(cf, Bytes.toBytes("a18"))),
        Bytes.toInt(r.getValue(cf, Bytes.toBytes("a19"))),
        Bytes.toInt(r.getValue(cf, Bytes.toBytes("a20"))),
        Bytes.toInt(r.getValue(cf, Bytes.toBytes("a21"))),
        Bytes.toInt(r.getValue(cf, Bytes.toBytes("a22"))),
        Bytes.toInt(r.getValue(cf, Bytes.toBytes("a23"))),
        Bytes.toInt(r.getValue(cf, Bytes.toBytes("a24"))),
        Bytes.toInt(r.getValue(cf, Bytes.toBytes("a25"))),
        Bytes.toInt(r.getValue(cf, Bytes.toBytes("a26"))),
        Bytes.toInt(r.getValue(cf, Bytes.toBytes("a27"))),
        Bytes.toInt(r.getValue(cf, Bytes.toBytes("a28"))),
        Bytes.toInt(r.getValue(cf, Bytes.toBytes("a29"))),
        Bytes.toInt(r.getValue(cf, Bytes.toBytes("a30")))
      )
  }

  @State(Scope.Thread)
  class Data {
    @Param(Array("10", "30"))
    var size: Int = _

    private[this] var cells: ju.List[Cell] = _
    var map: Map[String, Map[String, Int]] = _

    @Setup(Level.Iteration)
    def setup(): Unit = {
      val cs = (1 to size).map(i => cell(f"a$i%02d", Bytes.toBytes(i)))
      cells = Utils.toJavaList(cs)

      map = Map("cf1" -> (1 to size).map(i => f"a$i%02d" -> i).toMap)
    }

    def row: Array[Byte] = Bytes.toBytes("row")
    def cf1: Array[Byte] = Bytes.toBytes("cf1")
    def newPut: Put      = new Put(row)

    def cell(q: String, v: Array[Byte]): Cell =
      ExtendedCellBuilderFactory
        .create(CellBuilderType.DEEP_COPY)
        .setRow(row)
        .setFamily(cf1)
        .setQualifier(Bytes.toBytes(q))
        .setTimestamp(Long.MaxValue)
        .setType(Cell.Type.Put)
        .setValue(v)
        .build()

    val tc10 = Table(Columns10())
    val tc30 = Table(Columns30())

    def genResult: Result = Result.create(cells)

    lazy val decode10: Decoder[Table[Columns10]] = Decoder[Table[Columns10]]
    lazy val decode30: Decoder[Table[Columns30]] = Decoder[Table[Columns30]]

    lazy val encode10: PutEncoder[Table[Columns10]] = PutEncoder[Table[Columns10]]
    lazy val encode30: PutEncoder[Table[Columns30]] = PutEncoder[Table[Columns30]]
  }
}
