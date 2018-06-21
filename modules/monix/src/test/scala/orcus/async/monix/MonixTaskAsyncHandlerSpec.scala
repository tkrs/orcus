package orcus.async
package monix

import java.util.concurrent.{CancellationException, CompletableFuture, CompletionException, Executors}
import java.util.function.Supplier

import cats.~>
import _root_.monix.eval.Task
import _root_.monix.execution.Scheduler.Implicits.global

import scala.concurrent.Await
import scala.concurrent.blocking
import scala.concurrent.duration._

class MonixTaskAsyncHandlerSpec extends org.scalatest.FunSpec {

  describe("AsyncHandler[Task]") {
    it("should get a value as-is when its CompletableFuture is succeed") {
      def run(implicit f: CompletableFuture ~> Task) =
        f(CompletableFuture.completedFuture(10)).runAsync
      assert(10 === Await.result(run, 3.seconds))
    }
    it("should throw CompletionException as-is when its CompletableFuture is fail") {
      def run(implicit f: CompletableFuture ~> Task) =
        f(CompletableFuture.supplyAsync(new Supplier[Int] {
          def get(): Int = throw new Exception
        })).runAsync
      assertThrows[CompletionException](Await.result(run, 3.seconds))
    }
    it("should throw CancellationException as-is when its CompletableFuture was canceled") {
      val es = Executors.newSingleThreadExecutor()
      def run(implicit f: CompletableFuture ~> Task) =
        f(CompletableFuture.supplyAsync(new Supplier[Int] {
          def get(): Int = blocking { Thread.sleep(Int.MaxValue); fail() }
        }, es)).runAsync
      val f = run
      f.cancel()
      assertThrows[CancellationException](Await.result(f, 3.seconds))
    }
  }
}
