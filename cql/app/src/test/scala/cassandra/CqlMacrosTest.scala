package cassandra

import java.time.LocalDateTime._
import java.time.format.DateTimeFormatter.ISO_DATE_TIME

import cassandra.annotations.Id
import cassandra.cql._
import cassandra.cql.query.MatcherAdapter
import cassandra.cql.query.MatcherLike
import cassandra.cql.query.MatcherAdapter.Cql
import cassandra.fixtures.{FixtureFormats, Address, Person}
import cassandra.format.DataTypeFormat

import cassandra.query._
import org.joda.time.DateTimeZone.UTC
import org.joda.time.{DateTimeZone, DateTime}
import org.specs2.mutable.Specification
import FixtureFormats._

class CqlMacrosTest extends Specification {

  "CqlMacros" should {
    "writeCql" should {
      "produce the correct CqlType" in {
        val p = Person("john doe", 23, Address(12, "Lula Str", home = true), Some(165), List("Jack", "Black"), parse("2007-04-03T10:15:30.555Z", ISO_DATE_TIME))
        val cqlValue = personCqlValueFormat(p)
        cqlValue === CqlType("person",
          "name" -> CqlText("john doe"),
          "age" -> CqlNumber(23),
          "address" ->
            CqlType("address",
              "house" -> CqlNumber(12),
              "street" -> CqlText("Lula Str"),
              "home" -> CqlTrue
            ),
          "height" -> CqlNumber(165),
          "otherNames" -> CqlList(CqlText("Jack"), CqlText("Black")),
          "birthDate" -> CqlDateTime(new DateTime(2007, 4, 3, 10, 15, 30, 555, UTC)))

      }
    }
    "toUserDefinedType" should {
      "produce the correct CqlDataType" in {
        val userDefinedType = personCqlDataTypeFormat()
        userDefinedType === UserDefineDt(
          "person",
          List("name", "age", "birthDate"),
          "name" -> TextDt,
          "age" -> IntDt,
          "address" -> UserDefineDt("address", Nil ,"house" -> IntDt, "street" -> TextDt, "home" -> BooleanDt),
          "height" -> IntDt,
          "otherNames" -> ListDt(TextDt),
          "birthDate" -> TimestampDt)
      }
    }
    "handle tuples" in {

      val t2Format1 = tuple2Format[Int, String]
      t2Format1() === TupleDt(IntDt, TextDt)

      val t3Format = tuple3Format[Double, Int, String]
      t3Format() === TupleDt(DoubleDt, IntDt, TextDt)

      val t4Format = tuple4Format[Double, Int, String, Boolean]
      t4Format() === TupleDt(DoubleDt, IntDt, TextDt, BooleanDt)

      val t5Format = tuple5Format[Double, Int, String, Boolean, String]
      t5Format() === TupleDt(DoubleDt, IntDt, TextDt, BooleanDt, TextDt)

      val t6Format = tuple6Format[Double, Int, String, Boolean, Long, String]
      t6Format() === TupleDt(DoubleDt, IntDt, TextDt, BooleanDt, LongDt, TextDt)

      val t7Format = tuple7Format[Double, Int, String, Boolean, String, Double, Long]
      t7Format() === TupleDt(DoubleDt, IntDt, TextDt, BooleanDt, TextDt, DoubleDt, LongDt)
    }

    "Matcher macros" should {
      "produce matchers " in {
        Cql[Person](p => p.name == "ikea" && p.age < 18) === And(Eq("name", "ikea"), Lt("age", 18))
        Cql[Person](p => p.name == "ikea" && p.age < 18 && p.age >= 5) === And(And(Eq("name", "ikea"), Lt("age", 18)), GtEq("age", 5))
        Cql[Person](_.name == "ika") === Eq("name", "ika")
//        Cql2[Person](p => List("ika").contains(p.name)) === In("name", "ika")
        Cql[Person](_.age > 24) === Gt("age", 24)
        Cql[Person](_.age < 2) === Lt("age", 2)
        Cql[Person](_.age <= 4) === LtEq("age", 4)
        Cql[Person](_.age >= 25) === GtEq("age", 25)
      }
    }
  }

}