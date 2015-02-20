package cassandra.format

import com.datastax.driver.core.GettableByNameData

trait CqlDataReader[T, M] extends ((Option[String], GettableByNameData) => T) {
  def cqlClass:Class[M]
}
