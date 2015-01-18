package cassandra

import scala.reflect.macros.blackbox

class CqlMacros(val c:blackbox.Context) {
  import c.universe._

  def cqlFormatMacro[A: c.WeakTypeTag]: Tree = {
    val tpe:Type = c.weakTypeOf[A]

    val decls: MemberScope = tpe.decls
    val subseqs: List[Tree] = (decls collect {
      case method: MethodSymbol if method.isCaseAccessor =>
        q"(${method.name.encodedName.toString}, implicitly[CqlFormat[${method.returnType}]].apply(value.${method.name}))"
    }).toList

//    val appended: Tree =
//      subseqs.reduceLeft((a, b) => q"$a ++ $b")

    val cqlTypeName = tpe.baseClasses.head.name.toString.toLowerCase
    val cqlTypeParams = q"new String($cqlTypeName)" :: subseqs

    val cqlFormatter = q"""
    new CqlFormat[$tpe] {
      def apply(value: $tpe) = CqlType.apply(..$cqlTypeParams)
    }
    """
    println(showCode(cqlFormatter))
    cqlFormatter
  }

}
