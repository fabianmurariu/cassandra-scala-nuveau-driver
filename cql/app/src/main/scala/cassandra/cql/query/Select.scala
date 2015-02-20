package cassandra.cql.query

import com.datastax.driver.core.{SimpleStatement, Statement, Session, Row}
import play.api.libs.iteratee.{Iteratee, Enumerator}

import scala.collection.generic.CanBuildFrom
import scala.collection.mutable
import scala.concurrent.{Future, ExecutionContext}

trait Select {
  self: ResultSetHandler =>

  def select(query:String)
            (implicit session: Session, ex: ExecutionContext): SelectResult =
    select(new SimpleStatement(query))

  def select(query: Statement)
            (implicit session: Session, ex: ExecutionContext): SelectResult = {
    val eventualSet = executeAsync(query)
    new SelectResult(toEnumeratorTraversableBatch(eventualSet))
  }

}

class SelectResult(lzy: Enumerator[Stream[Row]]) {

  def collect[M <: TraversableOnce[Row]](implicit canBuildFrom:CanBuildFrom[M, Row, M], ex:ExecutionContext):Future[M] = {
    lzy |>>> Iteratee.fold(canBuildFrom()){
      (builder, rows: Stream[Row]) =>
        rows.foldLeft(builder){
          (b, row) => b += row
        }
    }.map(_.result())
  }

}

object Select extends Select with ResultSetHandler
