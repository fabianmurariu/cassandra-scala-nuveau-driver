package cassandra.dsl.words

import shapeless.{::, HList, HNil}

import scala.language.{higherKinds, implicitConversions}

trait Matcher[T <: HList] {
  def chain : T
  lazy val cql : String = ""
  lazy val prepCql : (String, List[_]) = ("", Nil)
}

abstract class BaseMatcher[T <: HList](val chain : T) extends Matcher[T]

object AnyMatcher extends Matcher[Nothing]{
  override def chain: Nothing = {throw new NoSuchElementException}
}
abstract class SimpleMatcher[T](name:String, v : T) extends BaseMatcher[T :: HNil](v :: HNil){
  val op:String
  override lazy val cql: String = s"$name $op ${toCql(chain.head)}"

  def toCql(value:Any) = value match {
    case _:String => s"'$value'"
    case _ => if (value != null) value.toString else "null"
  }

  override lazy val prepCql: (String, List[_]) = (s"$name $op ?", List(chain.head))
}

case class Eq[T](name:String, v:T) extends SimpleMatcher[T](name, v){
  override val op = "="
}
case class Gt[T](name:String, v:T) extends SimpleMatcher[T](name, v){
  override val op = ">"
}
case class GtEq[T](name:String, v:T) extends SimpleMatcher[T](name, v){
  override val op = ">="
}
case class Lt[T](name:String, v:T) extends SimpleMatcher[T](name, v){
  override val op = "<"
}
case class LtEq[T](name:String, v:T) extends SimpleMatcher[T](name, v){
  override val op = "<="
}
case class In[T](name:String, vs:List[T]) extends SimpleMatcher[Seq[T]](name, vs){
  override lazy val prepCql: (String, List[_]) = (s"""$name $op (${vs.map(_ => "?").mkString(",")})""", vs.toList)
  override lazy val cql: String = s"""$name $op (${vs.map(super.toCql).mkString(",")})"""
  override val op = "in"
}

object In{
  def apply[T](name:String, vs:T*):In[T] = In(name, vs.toList)
}

case class And[M1 <: Matcher[_ <: HList], M2 <: Matcher[_ <: HList]](m1:M1, m2:M2) extends BaseMatcher[M1 :: M2 :: HNil](m1 :: m2 :: HNil) {
  override lazy val cql: String = s"${chain.head.cql} AND ${chain.tail.head.cql}"
  override lazy val prepCql: (String, List[_]) = {

    (s"${chain.head.prepCql._1} AND ${chain.tail.head.prepCql._1}", chain.head.prepCql._2 ++ chain.tail.head.prepCql._2)
  }
}

//case class Or[M1 <: Matcher[_ <: HList], M2 <: Matcher[_ <: HList]](m1:M1, m2:M2) extends BaseMatcher[M1 :: M2 :: HNil](m1 :: m2 :: HNil)
case class Not[M <: Matcher[_ <: HList]](m1:M) extends BaseMatcher[M :: HNil](m1 :: HNil)