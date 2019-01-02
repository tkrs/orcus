package orcus.async

import java.util.concurrent.CompletableFuture
import java.util.function.Supplier

import org.scalatest.TestSuite

import scala.concurrent._

trait AsyncSpec { _: TestSuite =>

  def failedFuture[A]: CompletableFuture[A] =
    CompletableFuture.supplyAsync(new Supplier[A] {
      def get(): A = throw new Exception
    })

  def blockedFuture[A]: CompletableFuture[A] =
    CompletableFuture.supplyAsync(new Supplier[A] {
      def get(): A = blocking { Thread.sleep(Int.MaxValue); fail() }
    })

}
