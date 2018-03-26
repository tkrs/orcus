package orcus.async
package catseffect

import java.util.concurrent.{CompletableFuture, CompletionException}
import java.util.function.Supplier

import cats.effect.IO
import cats.~>

import scala.concurrent.duration._

class CatsEffectIOAsyncHandlerSpec extends org.scalatest.FunSpec {

  describe("AsyncHandler[Task]") {
    it("should get a value as-is when its CompletableFuture is succeed") {
      def run(implicit f: CompletableFuture ~> IO) =
        f(CompletableFuture.completedFuture(10))
      assert(10 === run.unsafeRunTimed(3.seconds).get)
    }
    it("should throw CompletionException as-is when its CompletableFuture is fail") {
      def run(implicit f: CompletableFuture ~> IO) =
        f(CompletableFuture.supplyAsync(new Supplier[Int] {
          def get(): Int = throw new Exception
        }))
      assertThrows[CompletionException](run.unsafeRunTimed(3.seconds))
    }
  }
}
