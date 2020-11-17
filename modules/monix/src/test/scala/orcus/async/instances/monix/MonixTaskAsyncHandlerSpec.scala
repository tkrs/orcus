package orcus.async.instances

import java.util.concurrent.CompletableFuture

import _root_.monix.eval.Task
import _root_.monix.execution.Scheduler.Implicits.global
import orcus.async.AsyncSpec
import orcus.async.Par
import orcus.async.implicits._
import orcus.async.instances.monix.task._
import org.scalatest.flatspec.AnyFlatSpec

import scala.concurrent.Await
import scala.concurrent.TimeoutException
import scala.concurrent.duration._

class MonixTaskAsyncHandlerSpec extends AnyFlatSpec with AsyncSpec {
  it should "convert to a Task" in {
    def run = Par[CompletableFuture, Task].parallel(CompletableFuture.completedFuture(10)).runToFuture
    assert(10 === Await.result(run, 1.second))
  }
  it should "convert to a failed Task" in {
    def run = Par[CompletableFuture, Task].parallel(failedFuture[Int](new Exception)).runToFuture
    assertThrows[Exception](Await.result(run, 1.second))
  }
  it should "convert to a cancelable Task" in {
    val source = blockedFuture[Int]
    val future = Par[CompletableFuture, Task].parallel(source).runToFuture
    future.cancel()
    assertThrows[TimeoutException](Await.result(future, 10.millis))
    assert(source.isCancelled)
  }
}
