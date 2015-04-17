package cassandra.dsl

import cassandra.query.{Matcher, AnyMatcher}
import shapeless.HList

sealed trait CqlLike
object EndLike extends CqlLike

trait SelectLike[T] extends CqlLike{
  def selectFrom[B <: T]:WhereLike[B] = new WhereLike[B]
}

trait InsertLike[T] extends CqlLike{
  def insertInto[B <: T]:WhereLike[B] = new WhereLike[B]
}

trait DeleteLike[T] extends CqlLike{
  def deleteFrom[B <: T]:WhereLike[B] = new WhereLike[B]
}

trait UpdateLike[T] extends CqlLike{
  def update[B <: T]:WhereLike[B] = new WhereLike[B]
}

class CountLike[T] extends CqlLike{
  def count() = EndLike
}

class WhereLike[T]() extends CqlLike{

  def where(t:T => Boolean):QueryAndLike[T] = new QueryAndLike[T](AnyMatcher)
}

class SetLike[T]() extends CqlLike{

  def set(t:T => Unit):QueryAndLike[T] = new QueryAndLike[T](AnyMatcher)
}

class QueryAndLike[T](m:Matcher[_ <: HList]) extends CqlLike{
  def and(t:T => Boolean):QueryAndLike[T] = new QueryAndLike[T](AnyMatcher)
  def count() = EndLike
}

class UsingLike(m:Matcher[_ <: HList]) extends CqlLike{
  def using(os:CqlOption[_]*) = EndLike
}
