package cassandra

import cassandra.annotations.Id
import cassandra.cql._
import cassandra.fixtures.{FixtureFormats, Address, Person}
import cassandra.format.DataTypeFormat
import org.specs2.mutable.Specification
import FixtureFormats._

class CqlMacrosTest extends Specification {

  "CqlMacros" should {
    "writeCql" should {
      "produce the correct CqlType" in {
        val p = Person("john doe", 23, Address(12, "Lula Str", home = true), Some(165), List("Jack", "Black"))
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
          "otherNames" -> CqlList(CqlText("Jack"), CqlText("Black")))

      }
    }
    "toUserDefinedType" should {
      "produce the correct CqlDataType" in {
        val userDefinedType = personCqlDataTypeFormat()
        userDefinedType === UserDefineDt(
          "person",
          List("name"),
          "name" -> TextDt,
          "age" -> IntDt,
          "address" -> UserDefineDt("address", Nil ,"house" -> IntDt, "street" -> TextDt, "home" -> BooleanDt),
          "height" -> IntDt,
          "otherNames" -> ListDt(TextDt))
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
  }

}