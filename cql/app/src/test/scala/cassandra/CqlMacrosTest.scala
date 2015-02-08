package cassandra

import cassandra.annotations.Id
import cassandra.cql._
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
  }

}

trait AWhatever

trait Another

case class Person(@Id name: String, age: Int, address: Address, height: Option[Int], otherNames: List[String]) extends AWhatever with Another

case class Address(house: Int, street: String, home: Boolean)
