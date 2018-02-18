package orcus.async.monix

import java.util.concurrent.{CompletableFuture, CompletionException, Executors}
import java.util.function.Supplier

import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import orcus.async.AsyncConversion

import scala.concurrent.{Await, CancellationException, blocking}
import scala.concurrent.duration._

class MonixTaskAsyncConversionSpec extends org.scalatest.FunSpec {

  describe("AsyncContext[Task]") {
    it("should get a value as-is when its CompletableFuture is succeed") {
      val f = AsyncConversion[Task].apply(CompletableFuture.completedFuture(10)).runAsync
      assert(10 === Await.result(f, 3.seconds))
    }
    it("should throw CompletionException as-is when its CompletableFuture is fail") {
      val f = AsyncConversion[Task]
        .apply(CompletableFuture.supplyAsync(new Supplier[Int] {
          def get(): Int = throw new Exception
        }))
        .runAsync
      assertThrows[CompletionException](Await.result(f, 3.seconds))
    }
    it("should throw CancellationException as-is when its CompletableFuture was canceled") {
      val es = Executors.newSingleThreadExecutor()
      val f = AsyncConversion[Task]
        .apply(CompletableFuture.supplyAsync(new Supplier[Int] {
          def get(): Int = blocking { Thread.sleep(Int.MaxValue); fail() }
        }, es))
        .runAsync
      f.cancel()
      assertThrows[CancellationException](Await.result(f, 3.seconds))
    }
  }
}
