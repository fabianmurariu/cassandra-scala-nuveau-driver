package cassandra.format

import cassandra.cql.CqlValue

trait CqlFormat[A] extends (A => CqlValue)