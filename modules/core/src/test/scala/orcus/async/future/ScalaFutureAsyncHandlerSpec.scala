package orcus.async.future

import java.util.concurrent.{CompletableFuture, CompletionException}

import orcus.async.{AsyncSpec, Par}
import org.scalatest.FunSpec

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class ScalaFutureAsyncHandlerSpec extends FunSpec with AsyncSpec {
  describe("AsyncHandler[scala.concurrent.Future]") {
    it("should get a value as-is when its CompletableFuture is succeed") {
      def run = Par[CompletableFuture, Future].parallel(CompletableFuture.completedFuture(10))
      assert(10 === Await.result(run, 3.seconds))
    }
    it("should throw CompletionException as-is when its CompletableFuture is fail") {
      def run = Par[CompletableFuture, Future].parallel(failedFuture[Int])
      assertThrows[CompletionException](Await.result(run, 3.seconds))
    }
  }
}
