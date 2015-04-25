package cassandra

import java.time.LocalDateTime._
import java.time.format.DateTimeFormatter.ISO_DATE_TIME

import cassandra.annotations.Id
import cassandra.cql._
import cassandra.dsl.words._
import cassandra.fixtures.{FixtureFormats, Address, Person}
import cassandra.format.DataTypeFormat

import org.joda.time.DateTimeZone.UTC
import org.joda.time.{DateTimeZone, DateTime}
import org.specs2.mutable.Specification
import FixtureFormats._

class CqlMacrosSpec extends Specification {

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
          "othernames" -> CqlList(CqlText("Jack"), CqlText("Black")),
          "birthdate" -> CqlDateTime(new DateTime(2007, 4, 3, 10, 15, 30, 555, UTC)))

      }
    }
    "toUserDefinedType" should {
      "produce the correct CqlDataType" in {
        val userDefinedType = personCqlDataTypeFormat()
        userDefinedType === UserDefineDt(
          "person",
          List("name", "age", "birthdate"),
          "name" -> TextDt,
          "age" -> IntDt,
          "address" -> UserDefineDt("address", Nil ,"house" -> IntDt, "street" -> TextDt, "home" -> BooleanDt),
          "height" -> IntDt,
          "othernames" -> ListDt(TextDt),
          "birthdate" -> TimestampDt)
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
        import cassandra.dsl.words.QueryLike

        val person = WhereLike[Person]()
        person.where(p => p.name == "ikea" && p.age < 18) === QueryLike(And(Eq("name", "ikea"), Lt("age", 18)))
        person.where(p => p.name == "ikea" && p.age < 18 && p.age >= 5) === QueryLike(And(And(Eq("name", "ikea"), Lt("age", 18)), GtEq("age", 5)))
        person.where(_.name == "ika") === QueryLike(Eq("name", "ika"))
        person.where(_.age > 24) === QueryLike(Gt("age", 24))
        person.where(_.age < 2) === QueryLike(Lt("age", 2))
        person.where(_.age <= 4) === QueryLike(LtEq("age", 4))
        person.where(_.age >= 25) === QueryLike(GtEq("age", 25))
      }
    }
  }

}