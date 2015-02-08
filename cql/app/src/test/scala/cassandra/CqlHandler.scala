package cassandra

import cassandra.cql.{UserDefineDt, CqlType, CqlTable}
import cassandra.format.{DataTypeFormat, CqlFormat}
import cassandra.statements.Statements

class CqlHandler[T](val t:T) extends AnyVal with Statements{
  def insert(implicit format:CqlFormat[T]):String = CqlTable(format(t).asInstanceOf[CqlType]).insert

}

class CqlTypeHandler[T](val ct:Class[T]) extends AnyVal with Statements{
  def createType(implicit format:DataTypeFormat[T]):String = {
    format().asInstanceOf[UserDefineDt].createType
  }
  def createTable(implicit format:DataTypeFormat[T]):String = format().asInstanceOf[UserDefineDt].createTable
}
