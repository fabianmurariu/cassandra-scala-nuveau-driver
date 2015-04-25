package cassandra.dsl

import cassandra.WhiteboxCqlMacros
import cassandra.cql.UserDefineDt
import cassandra.cql.query._
import cassandra.dsl.effects.Keyspace
import cassandra.query.{And, Matcher, AnyMatcher}
import com.datastax.driver.core.{DefaultPreparedStatement, Row, Session}
import shapeless.HList

import scala.concurrent.{Future, ExecutionContext}
import scala.language.experimental.macros
import scala.language.existentials

sealed trait CqlLike

object EndLike extends CqlLike

trait StatementLike extends CqlLike

trait Query[ORIG, OUT]

case class CqlQuery[ORIG, OUT]() extends Query[ORIG, OUT]

case class Select[TABLE](queryLike: QueryLike[TABLE]) extends StatementLike with BasicSelect with ResultSetHandler{

  val SELECT = "SELECT * FROM "

  def one(implicit table: Table2[TABLE]): Future[Option[TABLE]] = {
    val (where, values) = queryLike.ms.prepCql
    val cqlTableName = table.cqlDataTypeFormat().asInstanceOf[UserDefineDt].userDefinedName
    val query = s"$SELECT $cqlTableName WHERE $where"
    val javaValues = values.asInstanceOf[Seq[_ <: AnyRef]]

    implicit val (session, ex) = table.execParams

    for {
      rows:List[Row] <- select(table.keyspace.session.prepare(query).bind(javaValues:_*)).collect[List[Row]]
      rowO = rows.headOption
    } yield rowO.map(row => table.cqlDataReader.apply(Some(cqlTableName), row))
  }

  def range(page: Range)(implicit table: Table2[TABLE]): Future[List[TABLE]] = ???

  def count(implicit table: Table2[TABLE]):Future[Long] = ???

  def all(implicit table: Table2[TABLE]): Future[List[TABLE]] = ???

  def rows(implicit table: Table2[TABLE]): Future[List[Row]] = ???
}

trait SelectLike {
  self: Keyspace =>

  def selectFrom[B](queryAndLike: WhereLike[B] => QueryLike[B]): Select[B] = Select(queryAndLike(WhereLike[B]()))
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
