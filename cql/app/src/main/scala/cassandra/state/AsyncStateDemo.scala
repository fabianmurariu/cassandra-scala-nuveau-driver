//package cassandra.state
//
////import scala.concurrent.{Await, Future}
//import scala.concurrent.duration._
//import scala.language.postfixOps
//import scalaz._
//import Scalaz._
//import scalaz.concurrent.Future
//import scalaz.effect.IO
//
//object AsyncStateDemo extends App{
//
//  type AsyncStateInt[A] = IndexedStateT[Future, Int, Int, A]
//
//  val asyncState:AsyncStateInt[String] =
//    IndexedStateT[Future, Int, Int, String]( (number: Int) => Future.now((number, s"hello$number")))
//
//  def getMyFuckingState() = for {
//    helloText <- asyncState
//    anotherText <- IndexedStateT[Future, Int, Int, String]((number:Int) => Future.now((number - 1, "freak out")))
//  } yield {
//      anotherText + " Monad Transformers are awesome"
//    }
//
//  val doIt = getMyFuckingState()
//  println(doIt.eval(5).run)
//  println(doIt.exec(5).run)
//}
