package orcus.async.future

import java.util.concurrent.CompletableFuture

import orcus.async.{AsyncSpec, Par}
import orcus.async.implicits._
import orcus.async.instances.future._
import org.scalatest._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class ScalaFutureAsyncHandlerSpec extends FlatSpec with AsyncSpec {
  it should "convert to a Future" in {
    def run = Par[CompletableFuture, Future].parallel(CompletableFuture.completedFuture(10))
    assert(10 === Await.result(run, 3.seconds))
  }
  it should "convert to a failed Future" in {
    def run = Par[CompletableFuture, Future].parallel(failedFuture[Int](new Exception))
    assertThrows[Exception](Await.result(run, 3.seconds))
  }
}
