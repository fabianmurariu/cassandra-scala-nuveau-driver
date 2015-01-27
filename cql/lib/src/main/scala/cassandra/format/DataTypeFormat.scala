package cassandra.format

import cassandra.cql.CqlDataType

trait DataTypeFormat[T] extends (() => CqlDataType)

