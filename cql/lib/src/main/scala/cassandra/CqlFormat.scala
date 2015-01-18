package cassandra

import cassandra.Cql.CqlValue

trait CqlFormat[A] extends (A => CqlValue)