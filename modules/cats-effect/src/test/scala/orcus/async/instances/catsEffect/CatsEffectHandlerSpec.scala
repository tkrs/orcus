package orcus.async.instances.catsEffect

import java.util.concurrent.{CompletableFuture, CompletionException}

import cats.effect.IO
import orcus.async._
import orcus.async.implicits._
import effect._
import org.scalatest.FunSpec

class CatsEffectHandlerSpec extends FunSpec with AsyncSpec {
  describe("AsyncHandler[Effect[F]") {
    it("should get a value as-is when its CompletableFuture is succeed") {
      def run = Par[CompletableFuture, IO].parallel(CompletableFuture.completedFuture(10))
      assert(10 === run.unsafeRunSync())
    }
    it("should throw CompletionException as-is when its CompletableFuture is fail") {
      def run = Par[CompletableFuture, IO].parallel(failedFuture[Int])
      assertThrows[CompletionException](run.unsafeRunSync())
    }
  }
}
