package orcus.async.instances

import java.util.concurrent.CompletableFuture

import _root_.monix.eval.Task
import _root_.monix.execution.Scheduler.Implicits.global
import orcus.async.{AsyncSpec, Par}
import orcus.async.implicits._
import orcus.async.instances.monix.task._
import org.scalatest._

import scala.concurrent.duration._
import scala.concurrent.{Await, TimeoutException}
import org.scalatest.FlatSpec

class MonixTaskAsyncHandlerSpec extends FlatSpec with AsyncSpec {
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
