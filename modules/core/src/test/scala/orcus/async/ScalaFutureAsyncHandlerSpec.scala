package orcus.async

import java.util.concurrent.{CompletableFuture, CompletionException}

import org.scalatest.FunSpec

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

class ScalaFutureAsyncHandlerSpec extends FunSpec with AsyncSpec {

  describe("AsyncHandler[scala.concurrent.Future]") {
    it("should get a value as-is when its CompletableFuture is succeed") {
      def run = Par[Future].parallel(CompletableFuture.completedFuture(10))
      assert(10 === Await.result(run, 3.seconds))
    }
    it("should throw CompletionException as-is when its CompletableFuture is fail") {
      def run = Par[Future].parallel(failedFuture[Int])
      assertThrows[CompletionException](Await.result(run, 3.seconds))
    }
  }
}
