package cassandra

import java.time.LocalDateTime.parse
import java.time.format.DateTimeFormatter.ISO_DATE_TIME

import cassandra.dsl.effects.Keyspace
import cassandra.fixtures.FixtureFormats._
import cassandra.fixtures.{Address, Person}
import cassandra.test.utils.Specs2Futures
import org.specs2.mutable.Specification

import scala.concurrent.Future
import scala.language.higherKinds

class CqlInteractionSpec extends Specification with Specs2Futures{

  sequential

  "cql" should {
    val cluster = CassandraCluster("localhost")
    val ks = Keyspace("tevinzi", cluster)

    implicit val (session, ex) = ks.params

    "0 create type and table" in {

      session.execute("DROP TABLE if EXISTS person;")
      session.execute("DROP TYPE if EXISTS address;")

      val address = Address(12, "Lula Str", home = true)
      val person = Person("john doe", 23, address, Some(165), List("Jack", "Black"), parse("2000-04-03T10:15:30.555Z", ISO_DATE_TIME))
      val person2 = Person("belula banana", 25, address, None, List("Phebe", "Buffet"), parse("2001-02-03T10:15:30.555Z", ISO_DATE_TIME))

      val createAddressType = classOf[Address].createType
      createAddressType === "CREATE TYPE address (" +
        "house int," +
        "street text," +
        "home boolean" +
        ");"
      val createPersonTable = classOf[Person].createTable
      createPersonTable === "CREATE TABLE person (" +
        "name text," +
        "age int," +
        "address frozen <address>," +
        "height int," +
        "othernames list<text>," +
        "birthdate timestamp," +
        "PRIMARY KEY (name,age,birthdate)" +
        ");"

      session.execute(createAddressType)
      session.execute(createPersonTable)

      val insertedPerson = session.execute(person.insert)
      val insertedPerson2 = session.execute(person2.insert)

      insertedPerson.wasApplied() === true
      insertedPerson2.wasApplied() === true

    }

    "1 find one item" >> {

      val p: Future[Option[Person]] = ks.selectFrom[Person](_.where(_.name == "john doe")).one
      val p2: Future[Option[Person]] = ks.selectFrom[Person]{_.where(p => p.name == "john doe" && p.age > 20 )}.one

      val expected = Person("john doe", 23, Address(12, "Lula Str", home = true), Some(165), List("Jack", "Black"), parse("2000-04-03T10:15:30.555Z", ISO_DATE_TIME))

      p.toValue === expected
      p2.toValue === expected

    }
  }

}
