package cassandra.query

import shapeless.ops.hlist.Prepend
import shapeless.{HNil, HList, ::}

import scala.language.{higherKinds, implicitConversions}

trait Matcher[T <: HList]

case class Eq[T](v:T, name:String) extends Matcher[T :: HNil]
case class Gt[T](v:T, name:String) extends Matcher[T :: HNil]
case class Lt[T](v:T, name:String) extends Matcher[T :: HNil]

case class And3[M1 <: Matcher[_ <: HList], M2 <: Matcher[_ <: HList]](m1:M1, m2:M2) extends Matcher[M1 :: M2 :: HNil]
case class Or[M1 <: Matcher[_ <: HList], M2 <: Matcher[_ <: HList]](m1:M1, m2:M2) extends Matcher[M1 :: M2 :: HNil]
case class Not[M <: Matcher[_ <: HList]](m1:M) extends Matcher[M :: HNil]