package cassandra.cql.query

import cassandra.cql.{UserDefineDt, QueryBuilder}
import cassandra.format.{DataTypeFormat, CqlFormat, CqlDataReader}
import cassandra.query.{Field, AnyMatcher, Matcher}
import com.datastax.driver.core.{DefaultPreparedStatement, Session, Row}
import shapeless.{HNil, HList, ::}

import scala.collection.generic.CanBuildFrom
import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds

trait Table[T] {

  val cqlDataReader: CqlDataReader[T]
//  val cqlFormat: CqlFormat[T]
  val cqlDataTypeFormat: DataTypeFormat[T]
  val session: Session
  val executionContext: ExecutionContext

  @inline def findOne[V <: HList]()(m:MatcherLike[T]): Future[Option[T]] = findOne(m.m)

  def findOne[V <: HList](m:Matcher[V]): Future[Option[T]] = {
    val (_, vars: List[Object]) = m.prepCql
    val select = QueryBuilder.select(Seq(cqlDataTypeFormat().asInstanceOf[UserDefineDt].userDefinedName), m)
    val preparedStatement = session.prepare(select)
    implicit val prepared = executionContext.prepare()
    val futureRows = BasicSelect.select(preparedStatement.bind(vars:_*))(session, prepared).collect[List[Row]]
    for {
      rows <- futureRows
    } yield rows.map(cqlDataReader(None, _)).headOption
  }

  def find[V <: HList, M <: TraversableOnce[T]](m: Matcher[V])(implicit canBuildFrom: CanBuildFrom[M, Row, M]): Future[M] = ???

  def insert[V <: HList](value: T)(m: Matcher[V]) = ???

  def update[F, V <: HList](field: Field[F])(m: Matcher[V]): Future[Unit] = ???

}

case class MatcherLike[M](m:Matcher[_ <: HList])
