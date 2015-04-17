package cassandra.dsl

trait CqlTable[T] extends SelectLike[T] with InsertLike[T] with UpdateLike[T] with DeleteLike[T]{

}
