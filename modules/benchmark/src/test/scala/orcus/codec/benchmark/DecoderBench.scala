package orcus.codec.benchmark

import java.util.concurrent.TimeUnit

import orcus.codec.Decoder
import org.openjdk.jmh.annotations._

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 10, time = 2)
@Threads(1)
@Fork(2)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
class DecoderBench extends TestData {

  @Benchmark
  def decodeToCaseClass: Either[Throwable, Table] =
    Decoder[Table].apply(result)

  @Benchmark
  def decodeToMap: Either[Throwable, Map[String, Columns]] =
    Decoder[Map[String, Columns]].apply(result)
}
