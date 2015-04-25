package cassandra.dsl.words

sealed trait CqlOption[T]{
  val value:T
}
case class TTL(value:Int) extends CqlOption[Int]
