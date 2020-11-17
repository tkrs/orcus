package orcus.async.benchmark

import java.util.concurrent._

import _root_.monix.eval.Task
import _root_.monix.execution.Scheduler
import cats.Traverse
import cats.effect.ContextShift
import cats.effect.IO
import cats.instances.vector._
import com.twitter.util.{Await => TAwait}
import com.twitter.util.{Future => TFuture}
import orcus.async._
import orcus.async.implicits._
import org.openjdk.jmh.annotations._

import scala.concurrent.ExecutionContext
import scala.concurrent.{Await => SAwait}
import scala.concurrent.{Future => SFuture}
import scala.util.Random

@State(Scope.Benchmark)
@BenchmarkMode(Array(Mode.Throughput))
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 10, time = 5)
@OutputTimeUnit(TimeUnit.SECONDS)
@Threads(1)
@Fork(
  value = 2,
  jvmArgs = Array(
    "-server",
    "-Xms2g",
    "-Xmx2g",
    "-XX:NewSize=1g",
    "-XX:MaxNewSize=1g",
    "-XX:InitialCodeCacheSize=512m",
    "-XX:ReservedCodeCacheSize=512m",
    "-XX:+UseParallelGC",
    "-XX:-UseBiasedLocking",
    "-XX:+AlwaysPreTouch"
  )
)
abstract class AsyncHandlerBenchmark {
  final val Xs: Vector[Int] = Vector.range(1, 50)
  final val Rnd: Random     = new Random

  @Param(Array("1", "2", "4", "8", "16", "32", "64", "0"))
  var threads: Int = _

  var backgroundService: ExecutorService = _

  def daemonThreadFactory: ThreadFactory =
    new ThreadFactory {
      def newThread(r: Runnable): Thread = {
        val t = new Thread(r)
        t.setDaemon(true)
        if (t.getPriority != Thread.NORM_PRIORITY) t.setPriority(Thread.NORM_PRIORITY)
        t
      }
    }

  @Setup
  def setup(): Unit =
    if (threads <= 0)
      backgroundService = Executors.newCachedThreadPool(daemonThreadFactory)
    else
      backgroundService = Executors.newFixedThreadPool(threads, daemonThreadFactory)

  @inline final def compute(i: Int): CompletableFuture[Int] =
    CompletableFuture.supplyAsync(
      () => Rnd.nextInt(i) / i,
      backgroundService
    )

  @TearDown
  def tearDown(): Unit = {
    backgroundService.shutdown()
    if (!backgroundService.awaitTermination(10, TimeUnit.SECONDS))
      backgroundService.shutdownNow()
  }
}

class CatsEffectAsyncHandler extends AsyncHandlerBenchmark {
  import orcus.async.instances.catsEffect.concurrent._

  import scala.concurrent.duration._

  implicit val ec: ExecutionContext =
    ExecutionContext.fromExecutorService(Executors.newWorkStealingPool())

  implicit val ctxShift: ContextShift[IO] = IO.contextShift(ec)

  @Benchmark
  def bench: Vector[Int] = {
    val p = implicitly[Par.Aux[CompletableFuture, IO]]
    val f = Traverse[Vector].traverse[IO, Int, Int](Xs)(i => p.parallel(compute(i)))
    SAwait.result(f.unsafeToFuture(), 10.seconds)
  }
}

class MonixTaskAsyncHandler extends AsyncHandlerBenchmark {
  import orcus.async.instances.monix.task._

  import scala.concurrent.duration._

  implicit val scheduler: Scheduler = Scheduler.computation()

  @Benchmark
  def bench: Vector[Int] = {
    val p = implicitly[Par.Aux[CompletableFuture, Task]]
    val f = Traverse[Vector].traverse[Task, Int, Int](Xs)(i => p.parallel(compute(i)))
    SAwait.result(f.runToFuture, 10.seconds)
  }
}

class ScalaFutureAsyncHandler extends AsyncHandlerBenchmark {
  import cats.instances.future._
  import orcus.async.instances.future._

  import scala.concurrent.duration._

  implicit val ec: ExecutionContext =
    ExecutionContext.fromExecutorService(Executors.newWorkStealingPool())

  @Benchmark
  def bench: Vector[Int] = {
    val p = implicitly[Par.Aux[CompletableFuture, SFuture]]
    val f = Traverse[Vector].traverse[SFuture, Int, Int](Xs)(i => p.parallel(compute(i)))
    SAwait.result(f, 10.seconds)
  }
}

class ScalaJavaConverter extends AsyncHandlerBenchmark {
  import cats.instances.future._

  import scala.compat.java8.FutureConverters._
  import scala.concurrent.duration._

  implicit val ec: ExecutionContext =
    ExecutionContext.fromExecutorService(Executors.newWorkStealingPool())

  @Benchmark
  def bench: Vector[Int] = {
    val f = Traverse[Vector].traverse[SFuture, Int, Int](Xs)(i => compute(i).toScala)
    SAwait.result(f, 10.seconds)
  }
}

class TwitterFutureAsyncHandler extends AsyncHandlerBenchmark {
  import com.twitter.conversions.DurationOps._
  import io.catbird.util._
  import orcus.async.instances.twitterUtil.future._

  @Benchmark
  def bench: Vector[Int] = {
    val p = implicitly[Par.Aux[CompletableFuture, TFuture]]
    val f = Traverse[Vector].traverse[TFuture, Int, Int](Xs)(i => p.parallel(compute(i)))
    TAwait.result(f, 10.seconds)
  }
}
