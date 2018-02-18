package orcus.async.catseffect

import java.util.concurrent.{CompletableFuture, CompletionException}
import java.util.function.Supplier

import cats.effect.IO
import orcus.async.AsyncConversion

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class CatsEffectIOAsyncConversionSpec extends org.scalatest.FunSpec {

  describe("AsyncContext[Task]") {
    it("should get a value as-is when its CompletableFuture is succeed") {
      val f = AsyncConversion[IO].apply(CompletableFuture.completedFuture(10))
      assert(10 === f.unsafeRunTimed(3.seconds).get)
    }
    it("should throw CompletionException as-is when its CompletableFuture is fail") {
      val f = AsyncConversion[IO]
        .apply(CompletableFuture.supplyAsync(new Supplier[Int] {
          def get(): Int = throw new Exception
        }))
      assertThrows[CompletionException](f.unsafeRunTimed(3.seconds))
    }
  }
}
