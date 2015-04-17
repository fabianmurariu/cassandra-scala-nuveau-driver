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

//      val and: And[Gt[Int], Lt[Int]] = And(gtAge, ltAge)
//      val and2: And[Gt[Int], Eq[String]] = And(gtAge, eqName)
//      val notEqName: Not[Eq[String]] = Not(eqName)
//      val notAnd: Not[And[Gt[Int], Lt[Int]]] = Not(and)
//      val notAnd2: Not[And[Gt[Int], Eq[String]]] = Not(and2)
//      val andNotAnd: And[Not[And[Gt[Int], Eq[String]]], Not[And[Gt[Int], Lt[Int]]]] = And(notAnd2, notAnd)
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

    "transform to Cql for And" in {
      And(Gt("age", 18), Lt("age", 25)).cql === "age > 18 AND age < 25"
      And(Gt("age", 18), Lt("age", 25)).prepCql === ("age > ? AND age < ?", List(18, 25))
      And(Eq("name","ikea"), And(Gt("age", 18), Lt("age", 25))).prepCql === ("name = ? AND age > ? AND age < ?", List("ikea", 18, 25))
    }

    "transform to Cql for In" in {
      In("name", "Huevo", "John", "Sergei").cql === "name in ('Huevo','John','Sergei')"
      In("name", "Huevo", "John", "Sergei").prepCql === ("name in (?,?,?)", List("Huevo", "John", "Sergei"))
    }
  }

}
