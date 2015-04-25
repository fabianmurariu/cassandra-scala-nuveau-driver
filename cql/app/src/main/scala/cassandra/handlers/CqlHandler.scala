package cassandra.handlers

trait CqlHandler[T] extends ReadHandler[T] with WriteHandler[T] with TypeHandler[T]
