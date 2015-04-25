package cassandra.dsl.words

import java.time.LocalDateTime

trait Dsl{

  implicit class ContainsLike(value:Any){
    def in(values:List[_]):Boolean = values.contains(value)
  }

  implicit class LocalDateOrdering(value:LocalDateTime) extends Ordered[LocalDateTime]{
    override def compare(that: LocalDateTime): Int = {
      if (value.isBefore(that)){
        -1
      } else if (value.isAfter(that)){
        1
      } else 0
    }
  }

}

object Dsl extends Dsl
