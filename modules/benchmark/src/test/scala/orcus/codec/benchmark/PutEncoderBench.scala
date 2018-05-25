package orcus.codec.benchmark

import java.util.concurrent.TimeUnit

import orcus.codec.PutEncoder
import org.apache.hadoop.hbase.client.Put
import org.openjdk.jmh.annotations._

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 10, time = 1)
@Threads(1)
@Fork(2)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
class PutEncoderBench {
  import States._

  @Benchmark
  def encodeFromCaseClass(data: Data): Put = {
    if (data.size == 10)
      PutEncoder[Table[Columns10]].apply(data.newPut, Table(Columns10()))
    else
      PutEncoder[Table[Columns30]].apply(data.newPut, Table(Columns30()))
  }

  @Benchmark
  def encodeFromCaseClassCachedEncoder(data: Data): Put = {
    if (data.size == 10)
      data.encode10(data.newPut, Table(Columns10()))
    else
      data.encode30(data.newPut, Table(Columns30()))
  }

  @Benchmark
  def encodeFromMap(data: Data): Put =
    PutEncoder[Map[String, Map[String, Int]]].apply(data.newPut, data.map)
}
