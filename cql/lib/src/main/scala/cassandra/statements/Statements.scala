package cassandra.statements

import cassandra.cql.{UserDefineDt, CqlDataType, CqlTable, CqlType}
import cassandra.format.DataTypeFormat

trait Statements {

  trait CqlStatement

  implicit class CqlInsert(column: CqlTable) extends CqlStatement {
    lazy val fields = s"(${column.ofType.fields.map(_._1).mkString(",")})"
    lazy val values = column.values
    lazy val insert = s"INSERT into ${column.ofType.name} $fields VALUES $values"
  }

  implicit class CqlCreate(cqlT: UserDefineDt) extends CqlStatement {
    private lazy val primaryKeys = {
      val ids = cqlT.ids.mkString(",")
      if (ids == "") ids else s",PRIMARY KEY ($ids)"
    }
    lazy val createTable = s"CREATE TABLE ${cqlT.userDefinedName} ($types$primaryKeys)"
    lazy val createType = s"CREATE TYPE ${cqlT.userDefinedName} ($types)"
    lazy val types:String = cqlT.types.map{case (name, cqlDataType) => s"$name ${cqlDataType.name}" }.mkString(",")
  }

}

object Statements extends Statements