package cassandra.cql.query

import cassandra.WhiteboxCqlMacros
import cassandra.query.Matcher
import shapeless.HList

import scala.language.experimental.macros

object MatcherAdapter {

  def cql[T](selector: T => Boolean): Matcher[_ <: HList] = macro WhiteboxCqlMacros.whiteboxToMatcherFromSelector[T]

}
