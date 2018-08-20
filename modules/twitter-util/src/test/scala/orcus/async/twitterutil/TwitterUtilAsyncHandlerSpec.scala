package orcus.async
package twitterutil

import java.util.concurrent.{CompletableFuture, CompletionException}

import com.twitter.util.{Await, Future}
import org.scalatest.FunSpec

class TwitterUtilAsyncHandlerSpec extends FunSpec with AsyncSpec {

  describe("AsyncHandler[Future]") {
    it("should get a value as-is when its CompletableFuture is succeed") {
      def run = Par[Future].parallel(CompletableFuture.completedFuture(10))
      assert(10 === Await.result(run))
    }
    it("should throw CompletionException as-is when its CompletableFuture is fail") {
      def run = Par[Future].parallel(failedFuture[Int])
      assertThrows[CompletionException](Await.result(run))
    }
  }
}
