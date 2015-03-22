package cassandra.cql.query

import cassandra.cql.{UserDefineDt, QueryBuilder}
import cassandra.format.{DataTypeFormat, CqlFormat, CqlDataReader}
import cassandra.query.{Field, AnyMatcher, Matcher}
import com.datastax.driver.core.{DefaultPreparedStatement, Session, Row}
import shapeless.HList

import scala.collection.generic.CanBuildFrom
import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds

trait Table[T] {

  val cqlDataReader: CqlDataReader[T]
//  val cqlFormat: CqlFormat[T]
  val cqlDataTypeFormat: DataTypeFormat[T]
  val session: Session
  val executionContext: ExecutionContext

  def findOne[V <: HList](m: Matcher[V]): Future[Option[T]] = {
    val (_, vars: List[Object]) = m.prepCql
    val select = QueryBuilder.select(Seq(cqlDataTypeFormat().asInstanceOf[UserDefineDt].userDefinedName), m)
    val preparedStatement = session.prepare(select)
    implicit val prepared = executionContext.prepare()
    val futureRows = Select.select(preparedStatement.bind(vars:_*))(session, prepared).collect[List[Row]]
    for {
      rows <- futureRows
    } yield rows.map(cqlDataReader(None, _)).headOption
  }

  def findOne(selector:T => Boolean) : Future[Option[T]] = {
    import cassandra.query.Eq
    findOne(MatcherAdapter.cql(selector))
  }

  def find[V <: HList, M <: TraversableOnce[T]](m: Matcher[V])(implicit canBuildFrom: CanBuildFrom[M, Row, M]): Future[M] = ???

  def insert[V <: HList](value: T)(m: Matcher[V] = AnyMatcher) = ???

  def update[F, V <: HList](field: Field[F])(m: Matcher[V] = AnyMatcher): Future[Unit] = ???

}

//class CqlTable[T](val cqlDataReader: CqlDataReader[T],
//                  val cqlFormat: CqlFormat[T],
//                  val session: Session,
//                  val executionContext: ExecutionContext) extends Table[T] {
//
//  override def findOne[V <: HList](m: Matcher[V]): Future[Option[T]] = {
//    val (query, vars) = m.prepCql
//    val preparedStatement = session.prepare(query)
//    implicit val prepared = executionContext.prepare()
//    val futureRows = Select.select(preparedStatement.bind(vars.seq))(session, prepared).collect[List[Row]]
//    for {
//      rows <- futureRows
//    } yield rows.map(cqlDataReader(None, _)).headOption
//  }
//
//  override def find[V <: HList, M <: TraversableOnce[T]](m: Matcher[V])
//                                                        (implicit canBuildFrom: CanBuildFrom[M, Row, M]): Future[M] = ???
//
//  override def update[F, V <: HList](field: Field[F])(m: Matcher[V]): Future[Unit] = ???
//
//  override def insert[V <: HList](value: T)(m: Matcher[V]): Unit = ???
//
//}
