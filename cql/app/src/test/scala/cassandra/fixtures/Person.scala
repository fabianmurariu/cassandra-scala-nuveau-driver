package cassandra.fixtures

import cassandra.annotations.Id

case class Person(@Id name: String, age: Int, address: Address, height: Option[Int], otherNames: List[String]) extends AWhatever with Another
trait AWhatever
trait Another
