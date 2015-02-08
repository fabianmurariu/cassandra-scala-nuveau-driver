package cassandra

import StupidFuturesAdapter._
import com.datastax.driver.core.ResultSet

import scala.concurrent.{ExecutionContext, Future}

class ResultSetM {

  class ResultSetM(val rsFutureO: Future[Option[ResultSet]]) {
    def next(implicit ex: ExecutionContext): ResultSetM = {
//      for {
//        resultSetO <- rsFutureO
//        if resultSetO.isDefined
//        _ <- resultSetO.get.fetchMoreResults()
//      } yield {
//        resultSetO
//      }
      ???
    }
  }
}
