package orcus.async.twitterUtil

import java.util.concurrent.CompletableFuture

import com.twitter.util.{Await, Future}
import orcus.async.{AsyncSpec, Par}
import orcus.async.implicits._
import orcus.async.instances.twitterUtil.future._
import org.scalatest._

import scala.concurrent.CancellationException

class TwitterUtilFutureAsyncHandlerSpec extends FlatSpec with AsyncSpec {
  it should "convert to a Future" in {
    def run = Par[CompletableFuture, Future].parallel(CompletableFuture.completedFuture(10))
    assert(10 === Await.result(run))
  }
  it should "convert to a failed Future" in {
    def run = Par[CompletableFuture, Future].parallel(failedFuture[Int](new Exception))
    assertThrows[Exception](Await.result(run))
  }
  it should "convert to a cancelable Future" in {
    val source = blockedFuture[Int]
    val future = Par[CompletableFuture, Future].parallel(source)
    future.raise(new CancellationException)
    assertThrows[CancellationException](Await.result(future))
    assert(source.isCancelled)
  }
}
