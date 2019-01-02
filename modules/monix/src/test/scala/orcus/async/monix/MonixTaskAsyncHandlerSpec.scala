package orcus.async
package monix

import java.util.concurrent.{CompletableFuture, CompletionException, Executors}
import java.util.function.Supplier

import _root_.monix.eval.Task
import _root_.monix.execution.Scheduler.Implicits.global
import org.scalatest.FunSpec

import scala.concurrent.duration._
import scala.concurrent.{Await, TimeoutException, blocking}

class MonixTaskAsyncHandlerSpec extends FunSpec with AsyncSpec {

  describe("AsyncHandler[Task]") {
    it("should get a value as-is when its CompletableFuture is succeed") {
      def run = Par[Task].parallel(CompletableFuture.completedFuture(10)).runToFuture
      assert(10 === Await.result(run, 1.millis))
    }
    it("should throw CompletionException as-is when its CompletableFuture is fail") {
      def run = Par[Task].parallel(failedFuture[Int]).runToFuture
      assertThrows[CompletionException](Await.result(run, 1.millis))
    }

    describe("Canceling Future") {
      it("should be canceled") {
        val es = Executors.newSingleThreadExecutor()
        val blockedFuture = CompletableFuture.supplyAsync(new Supplier[Int] {
          def get(): Int = blocking { Thread.sleep(Int.MaxValue); fail() }
        }, es)
        val f = Par[Task].parallel(blockedFuture).runToFuture
        f.cancel()
        assertThrows[TimeoutException](Await.result(f, 1.millis))
        assert(blockedFuture.isCancelled)
      }
    }
  }
}
