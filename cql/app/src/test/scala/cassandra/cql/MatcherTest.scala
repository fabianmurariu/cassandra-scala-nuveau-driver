package cassandra.cql

import cassandra.fixtures.Person
import cassandra.query._
import org.specs2.mutable.Specification
import shapeless.{HNil,::}
class MatcherTest extends Specification{

  "Matchers" should {
    "can haz all tipez" in {
      val gtAge = Gt("age", 18)
      val ltAge = Lt("age", 25)
      val eqName = Eq("name", "Jane")
      val in = In("surname", List("blarg, bloom"))

      val and: And[Gt[Int], Lt[Int]] = And(gtAge, ltAge)
      val and2: And[Gt[Int], Eq[String]] = And(gtAge, eqName)
      val notEqName: Not[Eq[String]] = Not(eqName)
      val notAnd: Not[And[Gt[Int], Lt[Int]]] = Not(and)
      val notAnd2: Not[And[Gt[Int], Eq[String]]] = Not(and2)
      val andNotAnd: And[Not[And[Gt[Int], Eq[String]]], Not[And[Gt[Int], Lt[Int]]]] = And(notAnd2, notAnd)
      success("type checks")
    }

    "transform to Cql for Eq" in {
      Eq("name", "Jane").cql === "name = 'Jane'"
      Eq("name", "Jane").prepCql === ("name = ?", List("Jane"))
    }

    "transform to Cql for Gt" in {
      Gt("age", 5).cql === "age > 5"
      Gt("age", 5).prepCql === ("age > ?", List(5))
    }

    "transform to Cql for Lt" in {
      Lt("age", 5).cql === "age < 5"
      Lt("age", 5).prepCql === ("age < ?", List(5))
    }

    "transform to Cql for GtEq" in {
      GtEq("age", "5").cql === "age >= '5'"
      GtEq("age", "5").prepCql === ("age >= ?", List("5"))
    }

    "transform to Cql for LtEq" in {
      LtEq("age", 5).cql === "age <= 5"
      LtEq("age", 5).prepCql === ("age <= ?", List(5))
    }
  }

}
