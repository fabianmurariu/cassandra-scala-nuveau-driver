package cassandra.cql

import cassandra.fixtures.Person
import cassandra.query._
import org.specs2.mutable.Specification
import shapeless.{HNil,::}
class QueryBuilderTest extends Specification{

  "Matchers" should {
    "can haz all tipez" in {
      val gtAge = Gt(18, "age")
      val ltAge = Lt(25, "age")
      val eqName = Eq("Jane", "name")
      val and: And3[Gt[Int], Lt[Int]] = And3(gtAge, ltAge)
      val and2: And3[Gt[Int], Eq[String]] = And3(gtAge, eqName)
      val notEqName: Not[Eq[String]] = Not(eqName)
      val notAnd: Not[And3[Gt[Int], Lt[Int]]] = Not(and)
      val notAnd2: Not[And3[Gt[Int], Eq[String]]] = Not(and2)
      val andNotAnd: And3[Not[And3[Gt[Int], Eq[String]]], Not[And3[Gt[Int], Lt[Int]]]] = And3(notAnd2, notAnd)
      pending
    }
  }

}
