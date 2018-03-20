package orcus.codec.benchmark

import java.util.concurrent.TimeUnit

import orcus.codec.Decoder
import org.apache.hadoop.hbase.util.Bytes
import org.openjdk.jmh.annotations._

import scala.collection.mutable
import scala.util.control.NonFatal

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 10, time = 1)
@Threads(1)
@Fork(2)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
class DecoderBench extends TestData {

  @Benchmark
  def decodeToCaseClass: Table[Columns] =
    Decoder[Table[Columns]].apply(resultForColumns) match {
      case Right(v) => v
      case Left(e)  => throw e
    }

  @Benchmark
  def decodeToMap: Map[String, Columns] =
    Decoder[Map[String, Columns]].apply(resultForColumns) match {
      case Right(v) => v
      case Left(e)  => throw e
    }

  @Benchmark
  def decodeSelf: Map[String, Columns] =
    try {
      val cf = Bytes.toBytes("cf1")
      val m  = mutable.Map.empty[String, Columns]
      val r  = resultForColumns
      val c = Columns(
        Option(r.getValue(cf, Bytes.toBytes("a"))).map(Bytes.toInt),
        Option(r.getValue(cf, Bytes.toBytes("b"))).map(Bytes.toFloat),
        Option(r.getValue(cf, Bytes.toBytes("c"))).map(Bytes.toLong),
        Option(r.getValue(cf, Bytes.toBytes("d"))).map(Bytes.toDouble),
        Option(r.getValue(cf, Bytes.toBytes("e"))).map(Bytes.toString),
        Option(r.getValue(cf, Bytes.toBytes("g"))).map(Bytes.toBoolean),
        Option(r.getValue(cf, Bytes.toBytes("h"))).map(Bytes.toShort),
        Option(r.getValue(cf, Bytes.toBytes("i"))).map(Bytes.toBigDecimal(_))
      )
      m += "cf1" -> c
      m.toMap
    } catch {
      case NonFatal(e) => throw e
    }
}
