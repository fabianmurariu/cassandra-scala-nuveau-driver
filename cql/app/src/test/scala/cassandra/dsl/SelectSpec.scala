package cassandra.dsl

import java.time.LocalDateTime._
import java.time.format.DateTimeFormatter._

import cassandra.CassandraCluster
import cassandra.cql.query.Table2
import cassandra.dsl.effects.Keyspace
import cassandra.fixtures.{Address, Person}
import cassandra.format.{CqlDataReader, DataTypeFormat}
import cassandra.implicits.{CqlDataTypeImplicits, CqlReaderFormats}
import cassandra.test.utils.Specs2Futures
import org.specs2.mutable.Specification

import scala.concurrent.Future

class SelectSpec extends Specification with Specs2Futures{

  "select" should {
    "find one item" >> {

      import cassandra.query._

      val ks = Keyspace("tevinzi", CassandraCluster("localhost"))

      implicit val personTable = new Table2[Person] {
        import cassandra.cql.UserDefineDt
        import cassandra.implicits.CqlReaderFormats.UDTCqlDataReader
        import com.datastax.driver.core.GettableByNameData
        import cassandra.format.CqlDataReader
        import CqlReaderFormats._
        import CqlDataTypeImplicits._
        implicit def addressReader:CqlDataReader[Address] = CqlReaderFormats.cqlReaderFormat[Address]
        def cqlDataReader: CqlDataReader[Person] = CqlReaderFormats.cqlReaderFormat[Person]

        val keyspace: Keyspace = ks

        implicit def addressFormat: DataTypeFormat[Address] = CqlDataTypeImplicits.cqlDataTypeFormat[Address]
        def cqlDataTypeFormat: DataTypeFormat[Person] = CqlDataTypeImplicits.cqlDataTypeFormat[Person]
      }


      val p: Future[Option[Person]] = ks.selectFrom[Person](_.where(_.name == "john doe")).one
      val p2: Future[Option[Person]] = ks.selectFrom[Person]{_.where(p => p.name == "john doe" && p.age > 20 )}.one

      val expected = Person("john doe", 23, Address(12, "Lula Str", home = true), Some(165), List("Jack", "Black"), parse("2000-04-03T10:15:30.555Z", ISO_DATE_TIME))

      p.toValue === expected
      p2.toValue === expected

    }
  }

}
