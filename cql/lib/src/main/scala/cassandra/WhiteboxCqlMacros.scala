package cassandra

import cassandra.query.Matcher
import shapeless.HList

object WhiteboxCqlMacros {

  import scala.language.experimental.macros
  import scala.language.existentials

  import scala.reflect.macros.blackbox.{Context => BlackboxContext}

  def matcherFromSelector[A: c.WeakTypeTag](c: BlackboxContext)(selector: c.Tree) = {
    import c.universe._
    selector match {
      case q"(..$args) => $fbody" => matchFunctionBody[A](c)(fbody)
    }
  }

  def matchFunctionBody[A: c.WeakTypeTag](c: BlackboxContext)(selector: c.Tree) = {
    import c.universe._

    def makeSimpleMatcher(field: c.universe.NameApi, func: c.universe.NameApi, value: c.Tree) = {
      val termNameApi: NameApi = field
      val termName = termNameApi.decodedName.toString
      val funcName: NameApi = func
      funcName.decodedName.toString match {
        case "==" => q"Eq($termName, $value)"
        case ">" => q"Gt($termName, $value)"
        case "<" => q"Lt($termName, $value)"
        case "<=" => q"LtEq($termName, $value)"
        case ">=" => q"GtEq($termName, $value)"
      }
    }

    def breakDownSelector(innerSelector: c.Tree): c.Tree = {
      innerSelector match {
        case q"$x.$field.$func($value)" =>
          makeSimpleMatcher(field, func, value)
        case q"$x.$field.$func($value).&&($rest)" =>
          val leftMatcher = makeSimpleMatcher(field, func, value)
          val rightMatcher = breakDownSelector(rest)
          q"And($leftMatcher, $rightMatcher)"
      }
    }

    breakDownSelector(selector)
  }

}
