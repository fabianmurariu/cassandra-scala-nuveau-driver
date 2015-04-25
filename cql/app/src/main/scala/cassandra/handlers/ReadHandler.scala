package cassandra.handlers

import com.datastax.driver.core.{Row, GettableByNameData}

trait ReadHandler[T] {

  def read(data:Row):T

}


