package orcus.codec.benchmark

import java.util.concurrent.TimeUnit

import orcus.codec.Decoder
import orcus.codec.auto._
import org.apache.hadoop.hbase.util.Bytes
import org.openjdk.jmh.annotations._

import scala.collection.mutable

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 10, time = 1)
@Threads(1)
@Fork(2)
@OutputTimeUnit(TimeUnit.SECONDS)
class DecoderBench {
  import States._

  @Benchmark
  def decodeToCaseClass(data: Data): Either[Throwable, Table[Columns]] =
    if (data.size == 10)
      Decoder[Table[Columns10]].apply(data.genResult)
    else
      Decoder[Table[Columns30]].apply(data.genResult)

  @Benchmark
  def decodeToCaseClassCachedDecoder(data: Data): Either[Throwable, Table[Columns]] =
    if (data.size == 10)
      data.decode10(data.genResult)
    else
      data.decode30(data.genResult)

  @Benchmark
  def decodeToMap(data: Data): Either[Throwable, Map[String, Map[String, Int]]] =
    Decoder[Map[String, Map[String, Int]]].apply(data.genResult)

  @Benchmark
  def decodeSelf(data: Data): Map[String, Columns] = {
    val cf = Bytes.toBytes("cf1")
    val m  = mutable.Map.empty[String, Columns]
    val r  = data.genResult
    val c =
      if (data.size == 10) Columns10.fromResult(cf, r)
      else Columns30.fromResult(cf, r)
    m += "cf1" -> c
    m.toMap
  }
}
