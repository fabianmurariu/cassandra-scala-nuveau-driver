package cassandra

import java.time.{ZoneId, Instant, LocalDateTime}

import cassandra.annotations.Id
import play.api.libs.json._

package object fixtures{

  case class Person(@Id name: String,
                    @Id age: Int,
                    address: Address,
                    height: Option[Int],
                    othernames: List[String],
                    @Id birthdate:LocalDateTime) extends AWhatever with Another
  trait AWhatever
  trait Another

  case class Address(house: Int, street: String, home: Boolean)

  implicit val localDateTime:Reads[LocalDateTime] = new Reads[LocalDateTime] {
    override def reads(json: JsValue): JsResult[LocalDateTime] = json match {
      case JsNumber(value) => JsSuccess(LocalDateTime.ofInstant(Instant.ofEpochMilli(value.longValue()), ZoneId.of("UTC")))
    }
  }
  implicit val addressReads = Json.reads[Address]
  implicit val personReads = Json.reads[Person]

}