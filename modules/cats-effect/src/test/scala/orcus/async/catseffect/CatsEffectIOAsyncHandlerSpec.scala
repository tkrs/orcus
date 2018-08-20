package orcus.async
package catseffect

import java.util.concurrent.{CompletableFuture, CompletionException}

import cats.effect.IO
import org.scalatest.FunSpec

import scala.concurrent.duration._

class CatsEffectIOAsyncHandlerSpec extends FunSpec with AsyncSpec {

  describe("AsyncHandler[Task]") {
    it("should get a value as-is when its CompletableFuture is succeed") {
      def run = Par[IO].parallel(CompletableFuture.completedFuture(10))
      assert(10 === run.unsafeRunTimed(3.seconds).get)
    }
    it("should throw CompletionException as-is when its CompletableFuture is fail") {
      def run = Par[IO].parallel(failedFuture[Int])
      assertThrows[CompletionException](run.unsafeRunTimed(3.seconds))
    }
  }
}
