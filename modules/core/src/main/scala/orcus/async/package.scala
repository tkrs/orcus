package orcus

package object async extends JCompletableFutureHandler {

  type Callback[A] = (Either[Throwable, A] => Unit) => Unit
}
