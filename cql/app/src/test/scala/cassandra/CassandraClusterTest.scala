package cassandra

import cassandra.annotations.Id
import cassandra.cql.{CqlType, CqlTable, UserDefineDt}
import org.specs2.mutable.Specification

import cassandra.format.DataTypeFormat
import cassandra.format.CqlFormat
import cassandra.implicits.CqlDataTypeImplicits._
import cassandra.implicits.CqlImplicits._
import cassandra.statements.Statements._

class CassandraClusterTest extends Specification {

  "CassandraCluster" should {
    "connect" in {
      val cluster = CassandraCluster("localhost")
      cluster.connect
      cluster.close()
      true === true
    }
    "create type and table" in {
      val cluster = CassandraCluster("localhost")
      val session = cluster.connect("tevinzi")

      val address = Address(12, "Lula Str", home = true)
      val person = Person("john doe", 23, address, Some(165), List("Jack", "Black"))

      val addressType = toUserDefinedType(address).asInstanceOf[UserDefineDt]
      val personType = toUserDefinedType(person).asInstanceOf[UserDefineDt]

      val createAddressType = addressType.createType
      val createPersonTable = personType.createTable

      println(createAddressType)
      println(createPersonTable)

      session.execute ("DROP TABLE if EXISTS person;")
      session.execute ("DROP TYPE if EXISTS address;")

      session.execute(createAddressType)
      session.execute(createPersonTable)

      true === true // <- fix this once read is available
    }

    "insert into table" in {
      val cluster = CassandraCluster("localhost")
      val session = cluster.connect("tevinzi")

      val address = Address(12, "Lula Str", home = true)
      val person = Person("john doe", 23, address, Some(165), List("Jack", "Black"))

      val cqlPersonValue = writeCql(person) // <- need better
      val cqlPerson = CqlTable(cqlPersonValue.asInstanceOf[CqlType])

      println (cqlPerson.insert)
      session.execute(cqlPerson.insert)
      true === true // <- fix this once read is available
    }
  }
}