package cassandra.cql.query

import java.time.LocalDateTime._
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.ISO_DATE_TIME
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.SECONDS

import cassandra.{WhiteboxCqlMacros, CassandraCluster}
import cassandra.cql.query.MatcherAdapter.Cql
import cassandra.format.{DataTypeFormat, CqlDataReader}
import cassandra.query.{Matcher, And, Eq}
import com.datastax.driver.core.Session
import org.specs2.mutable.Specification
import cassandra.fixtures._
import shapeless.{HList, HNil}

import scala.concurrent.Await.result
import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

class TableTest extends Specification {

  "Table" should {
    "findOne" should {
      "support cassandra.query.Matcher" in {
        val cluster = CassandraCluster("localhost")
        val session = cluster.connect("tevinzi")
        val table = new PersonTable(
          FixtureFormats.personFormat2,
          FixtureFormats.personCqlDataTypeFormat,
          global, session)

        val personFO = table.findOne(And(Eq("name", "ikea"), Eq("age", 2)))

        session.close()

        result(personFO, Duration(5, SECONDS)) === Some(
          Person("ikea",
            2,
            Address(12, "Lula Str", home = true),
            Some(12), List("Jingle", "Baah"),
            parse("2009-06-03T10:15:30.555Z", ISO_DATE_TIME)))
      }

      "support functions via macros" in {
        val cluster = CassandraCluster("localhost")
        val session = cluster.connect("tevinzi")
        val table = new PersonTable(
          FixtureFormats.personFormat2,
          FixtureFormats.personCqlDataTypeFormat,
          global, session)

        val personFO = table.findOne()(Cql(_.name == "ikea"))

        session.close()

        result(personFO, Duration(5, SECONDS)) === Some(
          Person("ikea",
            2,
            Address(12, "Lula Str", home = true),
            Some(12), List("Jingle", "Baah"),
            parse("2009-06-03T10:15:30.555Z", ISO_DATE_TIME)))
      }
    }
  }

}

class PersonTable(val cqlDataReader: CqlDataReader[Person],
                  val cqlDataTypeFormat: DataTypeFormat[Person],
                  val executionContext: ExecutionContext,
                  val session: Session) extends Table[Person]
