package orcus.async
package twitterutil

import java.util.concurrent.{CompletableFuture, CompletionException}
import java.util.function.Supplier

import cats.~>
import com.twitter.util.{Await, Future}
import org.scalatest.FunSpec

class TwitterUtilAsyncHandlerSpec extends FunSpec {

  describe("AsyncHandler[Future]") {
    it("should get a value as-is when its CompletableFuture is succeed") {
      def run(implicit f: CompletableFuture ~> Future) =
        f(CompletableFuture.completedFuture(10))

      assert(10 === Await.result(run))
    }
    it("should throw CompletionException as-is when its CompletableFuture is fail") {
      def run(implicit f: CompletableFuture ~> Future) =
        f(CompletableFuture.supplyAsync(new Supplier[Int] {
          def get(): Int = throw new Exception
        }))

      assertThrows[CompletionException](Await.result(run))
    }
  }
}
