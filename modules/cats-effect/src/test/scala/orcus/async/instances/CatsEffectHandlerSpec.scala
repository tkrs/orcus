package orcus.async.instances

import java.util.concurrent.CompletableFuture

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import orcus.async._
import orcus.async.implicits._
import orcus.async.instances.catsEffect._
import org.scalatest.flatspec.AnyFlatSpec

import scala.concurrent.duration._

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
    Par[CompletableFuture, IO].parallel(f).start.flatMap(_.cancel).delayBy(1.second).unsafeRunSync()
    assert(f.isCancelled)
  }
}
