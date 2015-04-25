package cassandra.test.utils

import java.util.concurrent.TimeUnit.SECONDS
import java.util.concurrent.{TimeUnit}

import scala.concurrent.{Future, Await}
import scala.concurrent.duration.Duration

trait Specs2Futures {

  implicit class FutureHelper[T](f:Future[T]){
    def toValue:T = Await.result(f, Duration(15, SECONDS))
  }

  implicit class FutureOptionHelper[T](f:Future[Option[T]]) {
    def toValue:T = Await.result(f, Duration(15, SECONDS)).get
  }

}
