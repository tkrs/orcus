package orcus.async
package monix

import java.util.concurrent.{CompletableFuture, CompletionException}

import _root_.monix.eval.Task
import _root_.monix.execution.Scheduler.Implicits.global
import org.scalatest.FunSpec

import scala.concurrent.duration._
import scala.concurrent.{Await, TimeoutException}

class MonixTaskAsyncHandlerSpec extends FunSpec with AsyncSpec {

  describe("AsyncHandler[Task]") {
    it("should get a value as-is when its CompletableFuture is succeed") {
      def run = Par[CompletableFuture, Task].parallel(CompletableFuture.completedFuture(10)).runToFuture
      assert(10 === Await.result(run, 1.second))
    }
    it("should throw CompletionException as-is when its CompletableFuture is fail") {
      def run = Par[CompletableFuture, Task].parallel(failedFuture[Int]).runToFuture
      assertThrows[CompletionException](Await.result(run, 1.second))
    }

    describe("Canceling") {
      it("should be canceled") {
        val source = blockedFuture[Int]
        val future = Par[CompletableFuture, Task].parallel(source).runToFuture
        future.cancel()
        assertThrows[TimeoutException](Await.result(future, 10.millis))
        assert(source.isCancelled)
      }
    }
  }
}
