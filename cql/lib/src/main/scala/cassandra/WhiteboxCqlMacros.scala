package cassandra

object WhiteboxCqlMacros {

  import scala.language.existentials
  import scala.language.experimental.macros
  import scala.reflect.macros.blackbox.{Context => BlackboxContext}

  def queryAndLikeWithMatcher[A: c.WeakTypeTag](c: BlackboxContext)(selector: c.Tree) = {
    import c.universe._
    selector match {
      case q"(..$args) => $fbody" =>
        val tpe:Type = c.weakTypeOf[A]
        val matcher: c.Tree = makeMatcher[A](c)(fbody)
        q"import cassandra.dsl.words._;new QueryLike[$tpe]($matcher)"
    }
  }

  def makeMatcher[A: c.WeakTypeTag](c: BlackboxContext)(selector: c.Tree):c.Tree = {
    import c.universe._
    val tpe:Type = c.weakTypeOf[A]

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
        case "in" => q"In($termName, $value)"
      }
    }

    def expandTree(innerSelector: c.Tree):c.Tree = innerSelector match {
      case q"$x.$field.$func($value)" => makeSimpleMatcher(field, func, value)
      case q"$left.&&($right)" =>
        val leftTree = expandTree(left)
        val rightTree = expandTree(right)
        q"And($leftTree, $rightTree)"
      case q"$contains($x.$field).$func($value)" => makeSimpleMatcher(field, func, value)
    }

    expandTree(selector)
  }

}
