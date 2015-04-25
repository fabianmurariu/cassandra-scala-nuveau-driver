package cassandra.cql.query

import cassandra.dsl.effects.Keyspace
import cassandra.format.{CqlDataReader, DataTypeFormat}
import com.datastax.driver.core.Session

import scala.concurrent.ExecutionContext

trait Table2[T] {
  def cqlDataReader: CqlDataReader[T]
  //  val cqlFormat: CqlFormat[T]
  def cqlDataTypeFormat: DataTypeFormat[T]
  val keyspace:Keyspace
  def execParams:(Session, ExecutionContext) = (keyspace.session, keyspace.executionContext)
}
