package orcus.async.instances.catsEffect

import java.util.concurrent.CompletableFuture

import cats.effect.IO
import orcus.async._
import orcus.async.implicits._
import effect._
import org.scalatest._

class CatsEffectHandlerSpec extends FlatSpec with AsyncSpec {
  it should "convert to a IO" in {
    def run = Par[CompletableFuture, IO].parallel(CompletableFuture.completedFuture(10))
    assert(10 === run.unsafeRunSync())
  }
  it should "convert to a failed IO" in {
    def run = Par[CompletableFuture, IO].parallel(failedFuture[Int](new Exception))
    assertThrows[Exception](run.unsafeRunSync())
  }
}
