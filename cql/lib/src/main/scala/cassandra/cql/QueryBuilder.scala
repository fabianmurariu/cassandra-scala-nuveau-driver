package cassandra.cql

import cassandra.CqlMacros
import cassandra.query.Matcher

trait QueryBuilder {
//  def predicate[T](expr: T => Boolean): Matcher = macro CqlMacros.cql
}

object QueryBuilder extends QueryBuilder