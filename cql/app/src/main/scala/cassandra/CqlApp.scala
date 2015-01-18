package cassandra

import cassandra.Cql._

object CqlApp extends App{


  import cassandra.CqlImplicits._

  trait AWhatever
  trait Another
  case class Person(name: String, age: Int, address: Address, height:Option[Int], otherNames:List[String]) extends AWhatever with Another
  case class Address(house: Int, street: String, home:Boolean)

//  val cqlFormat = implicitly[CqlFormat[Person]]

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
  //
  //  val token = p.cql
  //  assert (token.query == "(name, age, address),('john doe', 23, {house: 12, street: 'Lula Str', home: true})")
  //  assert (token.types == List("name"->TCqlText, "age" -> TCqlInt))

//  new CqlFormat[Person] {
//    override def apply(v1: Person): CqlValue = CqlType(("name",implicitly[CqlFormat[String]].apply() )
//  }
}

