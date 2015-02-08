package cassandra

import cassandra.FixtureFormats._
import com.datastax.driver.core.SimpleStatement
import org.specs2.mutable.Specification

class ResultSetMTest extends Specification {

  "create type and table" in {
    val cluster = CassandraCluster("localhost")
    val session = cluster.connect("tevinzi")

    session.execute("DROP TABLE if EXISTS person;")
    session.execute("DROP TYPE if EXISTS address;")

    val address = Address(12, "Lula Str", home = true)
    val person = Person("john doe", 23, address, Some(165), List("Jack", "Black"))

    val createAddressType = classOf[Address].createType
    println(createAddressType)
    session.execute(createAddressType)
    val createPersonTable = classOf[Person].createTable
    println(createPersonTable)
    session.execute(createPersonTable)

    session.execute(person.insert)

    true === true // <- fix this once read is available
  }

  "trigger a query with a fetch size" in {
    val cluster = CassandraCluster("localhost")
    val session = cluster.connect("tevinzi")

    val address = Address(12, "Lula Str", home = true)
    val p1 = Person("john doe", 20, address, Some(165), List("Jack", "Black"))
    val p2 = Person("jane doe", 25, address, Some(113), List("Blerg", "Mlerg"))
    val p3 = Person("blarg blarg", 255, address, Some(12), List("Miaw", "Baah"))

    session.execute(p1.insert)
    session.execute(p2.insert)
    session.execute(p3.insert)

    val statement = new SimpleStatement("SELECT * from PERSON")
    statement.setFetchSize(1)

    session.executeAsync(statement)
    true === true
  }

}
