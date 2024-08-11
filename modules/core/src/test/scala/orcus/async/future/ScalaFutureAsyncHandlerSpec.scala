package orcus.async.future

import java.util.concurrent.CompletableFuture

import orcus.async.AsyncSpec
import orcus.async.Par
import orcus.async.implicits.*
import orcus.async.instances.future.*
import org.scalatest.flatspec.AnyFlatSpec

import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration.*

class ScalaFutureAsyncHandlerSpec extends AnyFlatSpec with AsyncSpec {
  it should "convert to a Future" in {
    def run = Par[CompletableFuture, Future].parallel(CompletableFuture.completedFuture(10))
    assert(10 === Await.result(run, 3.seconds))
  }
  it should "convert to a failed Future" in {
    def run = Par[CompletableFuture, Future].parallel(failedFuture[Int](new Exception))
    assertThrows[Exception](Await.result(run, 3.seconds))
  }
}
