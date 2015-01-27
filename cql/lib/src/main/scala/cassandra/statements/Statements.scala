package cassandra.statements

import cassandra.cql.{CqlTable, CqlType}
import cassandra.format.DataTypeFormat

trait Statements {

  trait CqlStatement

  implicit class CqlInsert(column: CqlTable) extends CqlStatement {
    lazy val fields = s"(${column.ofType.fields.map(_._1).mkString(",")})"
    lazy val values = column.values
    lazy val insert = s"INSERT into ${column.ofType.name} $fields VALUES $values"
  }

  implicit class CqlCreate(cqlT: CqlType) extends CqlStatement {
    lazy val createTable = s"CREATE TABLE ${cqlT.name}"

    def createType: String = s"CREATE TYPE ${cqlT.name}"
  }

}

object Statements extends Statements