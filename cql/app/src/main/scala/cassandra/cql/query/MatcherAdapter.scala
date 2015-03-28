package cassandra.cql.query

import cassandra.WhiteboxCqlMacros
import cassandra.query.Matcher
import shapeless.HList

import scala.language.experimental.macros

object MatcherAdapter {

  def Cql2[T](selector: T => Boolean): Matcher[_ <: HList] = macro WhiteboxCqlMacros.matcherFromSelector[T]

}
