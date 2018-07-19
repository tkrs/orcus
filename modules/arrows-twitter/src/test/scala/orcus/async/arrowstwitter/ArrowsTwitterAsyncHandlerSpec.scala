package orcus.async.arrowstwitter

import java.util.concurrent.{CompletableFuture, CompletionException}
import java.util.function.Supplier

import arrows.twitter.Task
import cats.~>
import com.twitter.util.Await
import orcus.async._
import org.scalatest.FunSpec

class ArrowsTwitterAsyncHandlerSpec extends FunSpec {

  describe("AsyncHandler[Task]") {
    it("should get a value as-is when its CompletableFuture is succeed") {
      def run(implicit f: CompletableFuture ~> Task) =
        f(CompletableFuture.completedFuture(10)).run

      assert(10 === Await.result(run))
    }
    it("should throw CompletionException as-is when its CompletableFuture is fail") {
      def run(implicit f: CompletableFuture ~> Task) =
        f(CompletableFuture.supplyAsync(new Supplier[Int] {
          def get(): Int = throw new Exception
        })).run

      assertThrows[CompletionException](Await.result(run))
    }
  }
}
