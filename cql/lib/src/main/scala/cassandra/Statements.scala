package cassandra

import cassandra.Cql.CqlTable

trait Statements {

  trait CqlStatement

  implicit class CqlInsert(column: CqlTable) {
    lazy val fields = s"(${column.ofType.fields.map(_._1).mkString(",")})"
    lazy val values = column.values
    lazy val insert = s"INSERT into ${column.ofType.name} $fields VALUES $values"
    lazy val createTable = s"CREATE TABLE ${column.ofType.name}"
    lazy val createType = ""
  }

}

object Statements extends Statements