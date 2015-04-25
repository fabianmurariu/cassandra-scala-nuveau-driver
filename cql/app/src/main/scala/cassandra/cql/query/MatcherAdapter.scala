package cassandra.cql.query

import cassandra.WhiteboxCqlMacros
import cassandra.dsl.{QueryLike, StatementLike}
import cassandra.query.Matcher
import shapeless.HList

import scala.language.experimental.macros

object MatcherAdapter {

  @inline def Cql[T](selector: T => Boolean): MatcherLike[T] = macro WhiteboxCqlMacros.matcherFromSelector[T]
//  @inline def matcher[T](stm:StatementLike, selector: T => Boolean): QueryAndLike[T] = macro WhiteboxCqlMacros.queryAndLikeWithMatcher[T]

}
