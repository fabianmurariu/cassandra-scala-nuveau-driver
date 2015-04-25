package cassandra

import java.time.LocalDateTime.parse
import java.time.format.DateTimeFormatter.ISO_DATE_TIME
import java.util.concurrent.TimeUnit

import cassandra.cql.query.{BasicSelect, BasicSelect$$, GuavaFuturesAdapter}
import cassandra.fixtures.FixtureFormats._
import cassandra.fixtures.{Address, FixtureFormats, Person}
import com.datastax.driver.core._
import org.specs2.mutable.Specification
import play.api.libs.iteratee.{Enumerator, Iteratee}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.language.higherKinds

class ResultSetMTest extends Specification {

    "create type and table" in {
      val cluster = CassandraCluster("localhost")
      val session = cluster.connect("tevinzi")

      session.execute("DROP TABLE if EXISTS person;")
      session.execute("DROP TYPE if EXISTS address;")

      val address = Address(12, "Lula Str", home = true)
      val person = Person("john doe", 23, address, Some(165), List("Jack", "Black"), parse("2000-04-03T10:15:30.555Z", ISO_DATE_TIME))

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
        "otherNames list<text>," +
        "birthDate timestamp," +
        "PRIMARY KEY (name,age,birthDate)" +
        ");"

      session.execute(createAddressType)
      session.execute(createPersonTable)

      val insertedPerson = session.execute(person.insert)
      session.close()
      insertedPerson.wasApplied() === true
    }

  "trigger a query with a fetch size" in {

    import cassandra.cql.query.GuavaFuturesAdapter._

    val cluster = CassandraCluster("localhost")
    implicit val session = cluster.connect("tevinzi")

    val address = Address(12, "Lula Str", home = true)
    val p1 = Person("black americano", 120, address, Some(165), List("Jack", "Turd"), parse("2007-04-03T10:15:30.555Z", ISO_DATE_TIME))
    val p2 = Person("sexy jade", 90, address, Some(113), List("Baah", "Mlerg"), parse("2008-05-03T10:15:30.555Z", ISO_DATE_TIME))
    val p3 = Person("ikea", 2, address, Some(12), List("Jingle", "Baah"), parse("2009-06-03T10:15:30.555Z", ISO_DATE_TIME))

    println(p1.insert)
    session.execute(p1.insert)
    session.execute(p2.insert)
    session.execute(p3.insert)

    val rowsF = BasicSelect.select("select * from PERSON").collect[List[Row]]
    val rows = Await.result(rowsF, Duration(15, TimeUnit.SECONDS))
    val personFormat = FixtureFormats.personFormat2
    val cassandraPersons = rows.map(row => personFormat(None, row))
    session.close()

    cassandraPersons must contain(p1, p2, p3)
  }

}
