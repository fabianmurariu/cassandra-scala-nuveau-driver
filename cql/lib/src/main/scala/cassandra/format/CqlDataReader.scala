package cassandra.format

import com.datastax.driver.core.GettableByNameData

trait CqlDataReader[T] extends ((Option[String], GettableByNameData) => T) {
  /* used to tell cassandra driver the type of the collection */
  def cqlClass:Class[_ <: Any]
}
