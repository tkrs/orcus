package orcus.async
package benchmark

import java.util.concurrent._
import java.util.function.Supplier

import cats.{Traverse, ~>}
import cats.instances.vector._
import org.openjdk.jmh.annotations._
import com.twitter.util.{Await => TAwait, Future => TFuture}
import _root_.monix.execution.Scheduler
import _root_.monix.eval.Task
import cats.effect.IO

import scala.concurrent.{ExecutionContext, Await => SAwait, Future => SFuture}

@State(Scope.Benchmark)
@BenchmarkMode(Array(Mode.Throughput))
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 10, time = 1)
@Threads(1)
@Fork(2)
@OutputTimeUnit(TimeUnit.SECONDS)
abstract class AsyncHandlerBenchmark {

  final val Xs: Vector[Int] = Vector.range(1, 50)

  @Param(Array("1", "2", "4", "8", "16", "32", "64", "0"))
  var threads: Int = _

  var backgroundService: ExecutorService = _

  def daemonThreadFactory: ThreadFactory = new ThreadFactory {
    def newThread(r: Runnable): Thread = {
      val t = new Thread(r)
      t.setDaemon(true)
      if (t.getPriority != Thread.NORM_PRIORITY) t.setPriority(Thread.NORM_PRIORITY)
      t
    }
  }

  @Setup
  def setup(): Unit = {
    if (threads <= 0)
      backgroundService = Executors.newCachedThreadPool(daemonThreadFactory)
    else
      backgroundService = Executors.newFixedThreadPool(threads, daemonThreadFactory)
  }

  final def compute: CompletableFuture[Int] =
    CompletableFuture.supplyAsync(new Supplier[Int] {
      def get(): Int = {
        TimeUnit.MILLISECONDS.sleep(10)
        10
      }
    }, backgroundService)

  @TearDown
  def tearDown(): Unit = {
    backgroundService.shutdown()
    if (!backgroundService.awaitTermination(10, TimeUnit.SECONDS)) {
      val _ = backgroundService.shutdownNow()
    }
  }
}

class CatsAsyncHandler extends AsyncHandlerBenchmark {
  import orcus.async.catseffect._
  import scala.concurrent.duration._

  implicit val ec: ExecutionContext =
    ExecutionContext.fromExecutorService(Executors.newWorkStealingPool())

  @Benchmark
  def bench: Vector[Int] = {
    val nat = implicitly[CompletableFuture ~> IO]
    val f   = Traverse[Vector].traverse[IO, Int, Int](Xs)(_ => nat.apply(compute))
    SAwait.result(f.unsafeToFuture(), 10.seconds)
  }
}

class MonixAsyncHandler extends AsyncHandlerBenchmark {
  import orcus.async.monix._
  import scala.concurrent.duration._

  implicit val scheduler: Scheduler = Scheduler.computation()

  @Benchmark
  def bench: Vector[Int] = {
    val nat = implicitly[CompletableFuture ~> Task]
    val f   = Traverse[Vector].traverse[Task, Int, Int](Xs)(_ => nat.apply(compute))
    SAwait.result(f.runAsync, 10.seconds)
  }
}

class ScalaAsyncHandler extends AsyncHandlerBenchmark {
  import cats.instances.future._
  import scala.concurrent.duration._

  implicit val ec: ExecutionContext =
    ExecutionContext.fromExecutorService(Executors.newWorkStealingPool())

  @Benchmark
  def bench: Vector[Int] = {
    val nat = implicitly[CompletableFuture ~> SFuture]
    val f   = Traverse[Vector].traverse[SFuture, Int, Int](Xs)(_ => nat.apply(compute))
    SAwait.result(f, 10.seconds)
  }
}

class ScalaJavaConverter extends AsyncHandlerBenchmark {
  import cats.instances.future._
  import scala.concurrent.duration._
  import scala.compat.java8.FutureConverters._

  implicit val ec: ExecutionContext =
    ExecutionContext.fromExecutorService(Executors.newWorkStealingPool())

  @Benchmark
  def bench: Vector[Int] = {
    val f = Traverse[Vector].traverse[SFuture, Int, Int](Xs)(_ => compute.toScala)
    SAwait.result(f, 10.seconds)
  }
}

class TwitterAsyncHandler extends AsyncHandlerBenchmark {
  import com.twitter.conversions.time._
  import io.catbird.util._
  import orcus.async.twitterutil._

  @Benchmark
  def bench: Vector[Int] = {
    val nat = implicitly[CompletableFuture ~> TFuture]
    val f   = Traverse[Vector].traverse[TFuture, Int, Int](Xs)(_ => nat.apply(compute))
    TAwait.result(f, 10.seconds)
  }
}
