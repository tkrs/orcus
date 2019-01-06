package orcus.async.arrowsTwitter.future

import java.util.concurrent.{CompletableFuture, CompletionException}

import arrows.twitter.Task
import com.twitter.util.Await
import orcus.async._
import org.scalatest.FunSpec

import scala.concurrent.CancellationException

class ArrowsTwitterFutureTaskAsyncHandlerSpec extends FunSpec with AsyncSpec {

  describe("AsyncHandler[Task]") {
    it("should get a value as-is when its CompletableFuture is succeed") {
      def run = Par[Task].parallel(CompletableFuture.completedFuture(10)).run
      assert(10 === Await.result(run))
    }
    it("should throw CompletionException as-is when its CompletableFuture is fail") {
      def run = Par[Task].parallel(failedFuture[Int]).run
      assertThrows[CompletionException](Await.result(run))
    }
    describe("Canceling") {
      it("should be canceled") {
        val source = blockedFuture[Int]
        val future = Par[Task].parallel(source).run
        future.raise(new CancellationException)
        assertThrows[CancellationException](Await.result(future))
        assert(source.isCancelled)
      }
    }
  }
}
