package cassandra

import cassandra.cql._
import cassandra.format.CqlFormat
import cassandra.implicits.CqlImplicits

object CqlApp extends App{


  import CqlImplicits._
  import cassandra.implicits.CqlDataTypeImplicits._

  trait AWhatever
  trait Another
  case class Person(name: String, age: Int, address: Address, height:Option[Int], otherNames:List[String]) extends AWhatever with Another
  case class Address(house: Int, street: String, home:Boolean)

  val p = Person("john doe", 23, Address(12, "Lula Str", home = true), Some(165), List("Jack", "Black"))

  val cqlType = writeCql(p)
  println(cqlType)
  assert( cqlType == CqlType("person",
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
    )

  println(cqlType)
}

