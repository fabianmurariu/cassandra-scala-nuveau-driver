package cassandra.dsl.words

import cassandra.WhiteboxCqlMacros
import cassandra.cql.query._
import cassandra.dsl.effects.Keyspace
import cassandra.dsl.words.Matcher
import cassandra.handlers.json.Cql2JsonReader
import cassandra.handlers.json.Cql2JsonReader.JsonReadsException
import com.datastax.driver.core.Row
import play.api.libs.json.Reads
import shapeless.HList

import scala.concurrent.Future
import scala.language.existentials
import scala.language.experimental.macros
import scala.reflect.ClassTag

sealed trait CqlLike

object EndLike extends CqlLike

trait StatementLike extends CqlLike

trait Query[ORIG, OUT]

case class CqlQuery[ORIG, OUT]() extends Query[ORIG, OUT]



trait SelectLike {
  self: Keyspace =>

  def selectFrom[B](queryAndLike: WhereLike[B] => QueryLike[B]): Select[B] =
    Select(queryAndLike(WhereLike[B]()))(self)
}

//object CqlInsert extends StatementLike
//trait InsertLike{
//  def insertInto[B]:WhereLike[B] = WhereLike[B]()
//}
//
//object CqlDelete extends StatementLike
//trait DeleteLike{
//  def deleteFrom[B]:WhereLike[B] = WhereLike[B]()
//}
//
//object CqlUpdate extends StatementLike
//trait UpdateLike{
//  def update[B]:WhereLike[B] = WhereLike[B]()
//}

case class WhereLike[T]() extends CqlLike {
  def where(selector: T => Boolean): QueryLike[T] = macro WhiteboxCqlMacros.queryAndLikeWithMatcher[T]
}

//class SetLike[T](stm:StatementLike) extends CqlLike{
//
//  def set(t:T => Unit):QueryAndLike[T] = new QueryAndLike[T](stm, AnyMatcher :: Nil)
//}

case class QueryLike[T](ms: Matcher[_ <: HList]) extends CqlLike {
  def count() = CqlQuery[T, Long]()

  def stream[OUT]: Query[T, OUT] = CqlQuery()
}

//class UsingLike(m: Matcher[_ <: HList]) extends CqlLike {
//  def using(os: CqlOption[_]*) = EndLike
//}
