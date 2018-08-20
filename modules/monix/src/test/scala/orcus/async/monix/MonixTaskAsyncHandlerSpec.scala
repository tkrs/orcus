package orcus.async
package monix

import java.util.concurrent.{CancellationException, CompletableFuture, CompletionException, Executors}
import java.util.function.Supplier

import _root_.monix.eval.Task
import _root_.monix.execution.Scheduler.Implicits.global
import org.scalatest.FunSpec

import scala.concurrent.Await
import scala.concurrent.blocking
import scala.concurrent.duration._

class MonixTaskAsyncHandlerSpec extends FunSpec with AsyncSpec {

  describe("AsyncHandler[Task]") {
    it("should get a value as-is when its CompletableFuture is succeed") {
      def run = Par[Task].parallel(CompletableFuture.completedFuture(10)).runAsync
      assert(10 === Await.result(run, 3.seconds))
    }
    it("should throw CompletionException as-is when its CompletableFuture is fail") {
      def run = Par[Task].parallel(failedFuture[Int]).runAsync
      assertThrows[CompletionException](Await.result(run, 3.seconds))
    }
    it("should throw CancellationException as-is when its CompletableFuture was canceled") {
      val es = Executors.newSingleThreadExecutor()
      val blockedFuture = CompletableFuture.supplyAsync(new Supplier[Int] {
        def get(): Int = blocking { Thread.sleep(Int.MaxValue); fail() }
      }, es)
      val f = Par[Task].parallel(blockedFuture).runAsync
      f.cancel()
      assertThrows[CancellationException](Await.result(f, 3.seconds))
    }
  }
}
