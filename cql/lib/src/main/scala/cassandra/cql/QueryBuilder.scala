package cassandra.cql

import cassandra.CqlMacros
import cassandra.query.Matcher
import shapeless.HList

trait QueryBuilder {
  def select[T <: HList](tables: Seq[String], matcher: Matcher[T]) = s"SELECT * FROM ${tables.mkString(",")} WHERE ${matcher.prepCql._1}"
}

object QueryBuilder extends QueryBuilder