package cassandra

import cassandra.query.Matcher
import shapeless.HList

object WhiteboxCqlMacros {

  import scala.language.experimental.macros

  import scala.reflect.macros.blackbox.{Context => BlackboxContext}
  import scala.reflect.macros.whitebox.{Context => WhiteboxContext}

  def whiteboxToMatcherFromSelector[A: c.WeakTypeTag](c: WhiteboxContext)(selector: c.Tree) = {
    import c.universe._
    println("########################## -> "+selector)
    println(showCode(selector))
    val result = selector match {
      case q"(..$args) => $x.$field.==($value)" =>
        val termName:NameApi = field
        q"Eq(${termName.decodedName.toString}, $value)"
      case q"(..$args) => $x.$field.>($value)" =>
        val termName:NameApi = field
        q"Gt(${termName.decodedName.toString}, $value)"
      case q"(..$args) => $x.$field.<($value)" =>
        val termName:NameApi = field
        q"Lt(${termName.decodedName.toString}, $value)"
      case q"(..$args) => $x.$field.>=($value)" =>
        val termName:NameApi = field
        q"GtEq(${termName.decodedName.toString}, $value)"
      case q"(..$args) => $x.$field.<=($value)" =>
        val termName:NameApi = field
        q"LtEq(${termName.decodedName.toString}, $value)"
//      case q"(..$args) => $body" =>
//        whiteboxToMatcherFromSelector[A](body)
      case _:Tree => q"AnyMatcher"
    }
    println(showCode(result))
    result
  }

}
