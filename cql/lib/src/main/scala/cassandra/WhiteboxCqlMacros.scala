package cassandra

import cassandra.query.Matcher

object WhiteboxCqlMacros {

  import scala.language.experimental.macros

  import scala.reflect.macros.blackbox.{Context => BlackboxContext}
  import scala.reflect.macros.whitebox.{Context => WhiteboxContext}

  def predicate[A](value: A => Boolean): Matcher[_] =
  macro whiteboxOptionOfMacro[A]

  def whiteboxOptionOfMacro[A: c.WeakTypeTag](c: WhiteboxContext)(value: c.Tree) = {
    import c.universe._
    q"Eq(25, blerg)"
  }

}
