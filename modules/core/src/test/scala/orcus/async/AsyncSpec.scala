package orcus.async
import java.util.concurrent.CompletableFuture
import java.util.function.Supplier

import org.scalatest.TestSuite

trait AsyncSpec { _: TestSuite =>

  def failedFuture[A]: CompletableFuture[A] =
    CompletableFuture.supplyAsync(new Supplier[A] {
      def get(): A = throw new Exception
    })
}
