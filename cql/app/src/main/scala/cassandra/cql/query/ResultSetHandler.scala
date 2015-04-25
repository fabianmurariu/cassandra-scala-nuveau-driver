package cassandra.cql.query

import cassandra.futures.GuavaFuturesAdapter
import GuavaFuturesAdapter._
import com.datastax.driver.core.{Session, Statement, Row, ResultSet}
import play.api.libs.iteratee.Enumerator

import scala.concurrent.{ExecutionContext, Future}

trait ResultSetHandler {

  def executeAsync(statement: Statement)
                  (implicit session: Session,
                   executionContext: ExecutionContext):Future[ResultSet] = session.executeAsync(statement)

  def grabUntilNextPageOrExhausted(rs: ResultSet): Stream[Row] = {
    if (rs.getAvailableWithoutFetching > 0 && !rs.isExhausted)
      rs.one #:: grabUntilNextPageOrExhausted(rs)
    else Stream.empty
  }

  def nextStream(rs: ResultSet)
                (implicit executionContext: ExecutionContext): Future[Stream[Row]] = for {
    _ <- rs.fetchMoreResults()
  } yield grabUntilNextPageOrExhausted(rs)

  def toEnumeratorTraversableBatch(rsF: Future[ResultSet])
                                  (implicit executionContext: ExecutionContext): Enumerator[Stream[Row]] =
    Enumerator.unfoldM(rsF) {
      rsF => for {
        rs <- rsF
        rows <- nextStream(rs)
      } yield {
        if (rs.isExhausted && rows.isEmpty) None
        else Some((rsF, rows))
      }
    }
}
