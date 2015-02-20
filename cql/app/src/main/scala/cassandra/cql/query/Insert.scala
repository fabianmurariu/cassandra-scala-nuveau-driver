package cassandra.cql.query

import cassandra.cql.{CqlTable, CqlType}
import cassandra.format.CqlFormat
import com.datastax.driver.core.{ResultSet, SimpleStatement, Session}
import cassandra.statements.Statements._
import scala.concurrent.{ExecutionContext, Future}

trait Insert { self:ResultSetHandler =>

  def insert[T](t:T)(implicit session:Session, cqlFormat: CqlFormat[T], ex: ExecutionContext):InsertResult = {
    val cqlValue = cqlFormat(t).asInstanceOf[CqlType]
    InsertResult(executeAsync(new SimpleStatement(CqlTable(cqlValue).insert)))
  }

}

object Insert extends Insert with ResultSetHandler

case class InsertResult(rsF:Future[ResultSet])
