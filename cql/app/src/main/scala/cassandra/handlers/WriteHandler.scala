package cassandra.handlers

import com.datastax.driver.core.Row

trait WriteHandler[T] {

  def write(t:T):Row

}
