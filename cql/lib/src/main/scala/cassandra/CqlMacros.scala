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
    val cqlTypeParams = q"String.valueOf($cqlTypeName)" :: subseqs

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
      case method:Symbol if method.annotations.exists(_.tree.tpe =:= typeOf[Id]) =>
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

  def cqlTupleTypeFormatMacro[A: c.WeakTypeTag]: Tree = {
    val tpe:Type = c.weakTypeOf[A]

    val typeArgs = tpe.typeArgs
    val formatArgs = typeArgs.map{
      argType => q"(implicitly[DataTypeFormat[$argType]]).apply()"
    }

    val tree = q"""
       class TupleFormat extends DataTypeFormat[(..$typeArgs)] {
         override def apply(): CqlDataType = {
           val l = List(..$formatArgs)
           TupleDt(l:_*)
         }
       }
       new TupleFormat()
     """
    println(showCode(tree))
    tree
  }

  def cqlRowReaderMacro[A: c.WeakTypeTag]: Tree = {
    val tpe:Type = c.weakTypeOf[A]

    val readers: List[Tree] = (tpe.decls collect {
      case method: MethodSymbol if method.isCaseAccessor =>
        val name = method.name.decodedName.toString
        q"""(implicitly[CqlDataReader[${method.returnType}]]).apply(Some(String.valueOf($name)),origin)"""
    }).toList

    val cqlFormatter = q"""
    new UDTCqlDataReader[$tpe] {
      override def apply(v1: Option[String], v2: GettableByNameData):$tpe = {
        val origin = getWithFallBack(v1, v2)
        new $tpe(..$readers)
      }
    }
    """
    println(showCode(cqlFormatter))
    cqlFormatter
  }





}
