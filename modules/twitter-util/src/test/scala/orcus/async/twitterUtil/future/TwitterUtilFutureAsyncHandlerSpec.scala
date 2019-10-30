package orcus.async.twitterUtil.future

import java.util.concurrent.{CompletableFuture, CompletionException}

import com.twitter.util.{Await, Future}
import orcus.async.{AsyncSpec, Par}
import org.scalatest.FunSpec

import scala.concurrent.CancellationException

class TwitterUtilFutureAsyncHandlerSpec extends FunSpec with AsyncSpec {

  describe("AsyncHandler[Future]") {
    it("should get a value as-is when its CompletableFuture is succeed") {
      def run = Par[CompletableFuture, Future].parallel(CompletableFuture.completedFuture(10))
      assert(10 === Await.result(run))
    }
    it("should throw CompletionException as-is when its CompletableFuture is fail") {
      def run = Par[CompletableFuture, Future].parallel(failedFuture[Int])
      assertThrows[CompletionException](Await.result(run))
    }
    describe("Canceling") {
      it("should be canceled") {
        val source = blockedFuture[Int]
        val future = Par[CompletableFuture, Future].parallel(source)
        future.raise(new CancellationException)
        assertThrows[CancellationException](Await.result(future))
        assert(source.isCancelled)
      }
    }
  }
}
