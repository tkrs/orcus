package orcus.codec.benchmark

import java.util.concurrent.TimeUnit

import orcus.codec.PutEncoder
import orcus.codec.generic.derived._
import org.apache.hadoop.hbase.client.Put
import org.openjdk.jmh.annotations._

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@Warmup(iterations = 10, time = 5)
@Measurement(iterations = 10, time = 10)
@Threads(1)
@Fork(2)
@OutputTimeUnit(TimeUnit.SECONDS)
class PutEncoderBench {
  import States._

  @Benchmark
  def encodeFromCaseClass(data: Data): Put = {
    if (data.size == 10)
      PutEncoder[Table[Columns10]].apply(data.newPut, data.tc10)
    else
      PutEncoder[Table[Columns30]].apply(data.newPut, data.tc30)
  }

  @Benchmark
  def encodeFromCaseClassCachedEncoder(data: Data): Put = {
    if (data.size == 10)
      data.encode10(data.newPut, data.tc10)
    else
      data.encode30(data.newPut, data.tc30)
  }

  @Benchmark
  def encodeFromMap(data: Data): Put =
    PutEncoder[Map[String, Map[String, Int]]].apply(data.newPut, data.map)
}
