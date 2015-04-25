package cassandra

import cassandra.annotations.Id

import scala.reflect.macros.blackbox

object CqlMacros {

  def cqlFormatMacro[A: c.WeakTypeTag](c:blackbox.Context): c.Tree = {
    import c.universe._
    val tpe:Type = c.weakTypeOf[A]

    val decls: MemberScope = tpe.decls
    val subseqs: List[Tree] = (decls collect {
      case method: MethodSymbol if method.isCaseAccessor =>
        q"(${method.name.encodedName.toString}, implicitly[CqlFormat[${method.returnType}]].apply(value.${method.name}))"
    }).toList

    val cqlTypeName = tpe.baseClasses.head.name.toString.toLowerCase
    val cqlTypeParams = q"String.valueOf($cqlTypeName)" :: subseqs

    q"""
    new CqlFormat[$tpe] {
      def apply(value: $tpe) = CqlType.apply(..$cqlTypeParams)
    }
    """
  }

  def cqlDataTypeFormatMacro[A: c.WeakTypeTag](c:blackbox.Context): c.Tree = {
    import c.universe._
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

    q"""
    new DataTypeFormat[$tpe] {
      def apply() = UserDefineDt.apply($cqlTypeName, List(..$ids), ..$cqlDataTypes)
    }
    """
  }

  def cqlTupleTypeFormatMacro[A: c.WeakTypeTag](c:blackbox.Context): c.Tree = {
    import c.universe._
    val tpe:Type = c.weakTypeOf[A]

    val typeArgs = tpe.typeArgs
    val formatArgs = typeArgs.map{
      argType => q"(implicitly[DataTypeFormat[$argType]]).apply()"
    }

    q"""
       class TupleFormat extends DataTypeFormat[(..$typeArgs)] {
         override def apply(): CqlDataType = {
           val l = List(..$formatArgs)
           TupleDt(l:_*)
         }
       }
       new TupleFormat()
     """
  }

}
