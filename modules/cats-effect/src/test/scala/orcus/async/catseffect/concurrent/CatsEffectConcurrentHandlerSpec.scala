package orcus.async.catsEffect.concurrent

import java.util.concurrent.{CompletableFuture, CompletionException}

import cats.effect.{ContextShift, IO, Timer}
import orcus.async._
import org.scalatest.FunSpec

import scala.concurrent._

class CatsEffectConcurrentHandlerSpec extends FunSpec with AsyncSpec {
  import ExecutionContext.Implicits.global

  implicit def contextShift: ContextShift[IO] =
    IO.contextShift(global)

  implicit def timer: Timer[IO] = IO.timer(global)

  describe("AsyncHandler[ConcurrentEffect[F]") {
    it("should get a value as-is when its CompletableFuture is succeed") {
      def run = Par[IO].parallel(CompletableFuture.completedFuture(10))
      assert(10 === run.unsafeRunSync())
    }
    it("should throw CompletionException as-is when its CompletableFuture is fail") {
      def run = Par[IO].parallel(failedFuture[Int])
      assertThrows[CompletionException](run.unsafeRunSync())
    }
    describe("Canceling") {
      it("should be canceled") {
        val f = blockedFuture[Int]
        Par[IO].parallel(f).start.flatMap(_.cancel).unsafeRunSync()
        assert(f.isCancelled)
      }
    }
  }
}
