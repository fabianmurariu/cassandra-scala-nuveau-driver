package cassandra.format

import com.datastax.driver.core.GettableByNameData

trait CqlDataReader[T] extends ((Option[String], GettableByNameData) => T) {
  def cqlClass:Class[_ <: Any]
}
