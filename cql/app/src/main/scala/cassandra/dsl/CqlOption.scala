package cassandra.dsl

sealed trait CqlOption[T]{
  val value:T
}
case class TTL(value:Int) extends CqlOption[Int]
