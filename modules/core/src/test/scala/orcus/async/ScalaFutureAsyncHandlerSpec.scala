package orcus.async

import java.util.concurrent.{CompletableFuture, CompletionException}
import java.util.function.Supplier

import cats.~>
import org.scalatest.FunSpec

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class ScalaFutureAsyncHandlerSpec extends FunSpec {

  describe("AsyncHandler[scala.concurrent.Future]") {
    it("should get a value as-is when its CompletableFuture is succeed") {
      def run(implicit f: CompletableFuture ~> Future) =
        f(CompletableFuture.completedFuture(10))

      assert(10 === Await.result(run, 3.seconds))
    }
    it("should throw CompletionException as-is when its CompletableFuture is fail") {
      def run(implicit f: CompletableFuture ~> Future) =
        f(CompletableFuture.supplyAsync(new Supplier[Int] {
          def get(): Int = throw new Exception
        }))

      assertThrows[CompletionException](Await.result(run, 3.seconds))
    }
  }
}
