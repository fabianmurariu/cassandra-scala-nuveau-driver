package cassandra.query

import shapeless.ops.hlist.Prepend
import shapeless.{HNil, HList, ::}

import scala.language.{higherKinds, implicitConversions}

trait Matcher[T <: HList] {
  val chain : T
  lazy val cql : String = ""
  lazy val prepCql : (String, List[_]) = ???
}

abstract class BaseMatcher[T <: HList](val chain : T) extends Matcher[T]

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
case class Gt[T:Ordering](name:String, v:T) extends SimpleMatcher[T](name, v){
  override val op = ">"
}
case class GtEq[T:Ordering](name:String, v:T) extends SimpleMatcher[T](name, v){
  override val op = ">="
}
case class Lt[T:Ordering](name:String, v:T) extends SimpleMatcher[T](name, v){
  override val op = "<"
}
case class LtEq[T:Ordering](name:String, v:T) extends SimpleMatcher[T](name, v){
  override val op = "<="
}
case class In[T](name:String, v:List[T]) extends BaseMatcher[List[T] :: HNil](v :: HNil)

case class And[M1 <: Matcher[_ <: HList], M2 <: Matcher[_ <: HList]](m1:M1, m2:M2) extends BaseMatcher[M1 :: M2 :: HNil](m1 :: m2 :: HNil)
case class Or[M1 <: Matcher[_ <: HList], M2 <: Matcher[_ <: HList]](m1:M1, m2:M2) extends BaseMatcher[M1 :: M2 :: HNil](m1 :: m2 :: HNil)
case class Not[M <: Matcher[_ <: HList]](m1:M) extends BaseMatcher[M :: HNil](m1 :: HNil)