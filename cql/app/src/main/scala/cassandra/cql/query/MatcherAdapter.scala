package cassandra.cql.query

import cassandra.WhiteboxCqlMacros
import cassandra.query.Matcher
import shapeless.HList

import scala.language.experimental.macros

object MatcherAdapter {

  @inline def Cql[T](selector: T => Boolean): MatcherLike[T] = macro WhiteboxCqlMacros.matcherFromSelector[T]

}
