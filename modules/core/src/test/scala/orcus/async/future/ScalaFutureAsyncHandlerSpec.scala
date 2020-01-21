package orcus.async.future

import java.util.concurrent.CompletableFuture

import orcus.async.implicits._
import orcus.async.instances.future._
import orcus.async.{AsyncSpec, Par}
import org.scalatest.flatspec.AnyFlatSpec

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

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
