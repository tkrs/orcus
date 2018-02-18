package orcus.async.twitterutil

import java.util.concurrent.{CompletableFuture, CompletionException, ExecutorService, Executors}
import java.util.function.Supplier

import com.twitter.util.{Await, Future}
import orcus.async.AsyncConversion

class TwitterUtilAsyncConversionSpec extends org.scalatest.FunSpec {

  implicit val executorService: ExecutorService =
    Executors.newSingleThreadExecutor()

  describe("AsyncContext[Future]") {
    it("should get a value as-is when its CompletableFuture is succeed") {
      val f = AsyncConversion[Future].apply(CompletableFuture.completedFuture(10))
      assert(10 === Await.result(f))
    }
    it("should throw CompletionException as-is when its CompletableFuture is fail") {
      val f = AsyncConversion[Future]
        .apply(CompletableFuture.supplyAsync(new Supplier[Int] {
          def get(): Int = throw new Exception
        }))
      assertThrows[CompletionException](Await.result(f))
    }
  }
}
