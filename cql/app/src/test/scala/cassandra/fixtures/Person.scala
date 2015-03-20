package cassandra.fixtures

import java.time.LocalDateTime

import cassandra.annotations.Id

case class Person(@Id name: String,
                  @Id age: Int,
                  address: Address,
                  height: Option[Int],
                  otherNames: List[String],
                  @Id birthDate:LocalDateTime ) extends AWhatever with Another
trait AWhatever
trait Another
