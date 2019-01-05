package orcus.async
package catsEffect

import java.util.concurrent.{CompletableFuture, CompletionException}

import cats.effect.IO
import org.scalatest.FunSpec

class CatsEffectHandlerSpec extends FunSpec with AsyncSpec {

  describe("AsyncHandler[Effect[F]") {
    it("should get a value as-is when its CompletableFuture is succeed") {
      def run = Par[IO].parallel(CompletableFuture.completedFuture(10))
      assert(10 === run.unsafeRunSync())
    }
    it("should throw CompletionException as-is when its CompletableFuture is fail") {
      def run = Par[IO].parallel(failedFuture[Int])
      assertThrows[CompletionException](run.unsafeRunSync())
    }
  }
}
