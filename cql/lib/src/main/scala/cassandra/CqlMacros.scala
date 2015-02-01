package cassandra

import cassandra.annotations.Id
import cassandra.cql.CqlValue
import cassandra.format.CqlFormat

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

    val cqlTypeName = tpe.baseClasses.head.name.toString.toLowerCase
    val cqlTypeParams = q"new String($cqlTypeName)" :: subseqs

    val cqlFormatter = q"""
    new CqlFormat[$tpe] {
      def apply(value: $tpe) = CqlType.apply(..$cqlTypeParams)
    }
    """
//    println(showCode(cqlFormatter))
    cqlFormatter
  }

  def cqlDataTypeFormatMacro[A: c.WeakTypeTag]: Tree = {
    val tpe:Type = c.weakTypeOf[A]

    val decls: MemberScope = tpe.decls
    val cqlDataTypes = decls collect {
      case method: MethodSymbol if method.isCaseAccessor =>
        q"""
           (${method.name.encodedName.toString},
           implicitly[DataTypeFormat[${method.returnType}]].apply())"""
    }
    val ids = decls collect {
      case method:Symbol if method.annotations.nonEmpty =>
        method.name.toString.trim
    }

    val cqlTypeName = tpe.baseClasses.head.name.toString.toLowerCase

    val cqlDataTypeFormatter = q"""
    new DataTypeFormat[$tpe] {
      def apply() = UserDefineDt.apply($cqlTypeName, List(..$ids), ..$cqlDataTypes)
    }
    """
    println(showCode(cqlDataTypeFormatter))
    cqlDataTypeFormatter
  }



}
