package orcus.async.instances

import java.util.concurrent.CompletableFuture

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import orcus.async.*
import orcus.async.implicits.*
import orcus.async.instances.catsEffect.*
import org.scalatest.flatspec.AnyFlatSpec

import scala.concurrent.duration.*

class CatsEffectHandlerSpec extends AnyFlatSpec with AsyncSpec {

  it should "convert to a IO" in {
    def run = Par[CompletableFuture, IO].parallel(CompletableFuture.completedFuture(10))
    assert(10 === run.unsafeRunSync())
  }
  it should "convert to a failed IO" in {
    def run = Par[CompletableFuture, IO].parallel(failedFuture[Int](new Exception))
    assertThrows[Exception](run.unsafeRunSync())
  }
  it should "convert to a cancelable IO" in {
    val f = blockedFuture[Int]

    val run = for {
      fiber <- Par[CompletableFuture, IO].parallel(f).start
      _     <- IO.sleep(3.seconds)
      _     <- fiber.cancel
    } yield ()

    run.unsafeRunSync()

    assert(f.isCancelled)
  }
}
