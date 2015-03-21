package cassandra.cql.query

import java.time.LocalDateTime._
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.SECONDS

import cassandra.fixtures._
import cassandra.fixtures.{FixtureFormats, Address, Person}
import FixtureFormats._
import cassandra.CassandraCluster
import com.datastax.driver.core.Row
import org.specs2.mutable.Specification

import scala.concurrent.Await.ready
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
class CassandraIntegrationTest extends Specification {

  "Insert then Query" should {
    "Insert into the database then read with macro formats" in {

      val address = Address(2, "Blula Bld", home = true)
      val p1 = Person("carlitos", 40, address, Some(165), List("Foo", "Marg"), parse("2007-04-03T10:15:30.555Z", DateTimeFormatter.ISO_DATE_TIME))

      val cluster = CassandraCluster("localhost")
      implicit val session = cluster.connect("tevinzi")

      val response = Insert.insert(p1)
      ready(response.rsF, Duration(15, SECONDS))

      val carlitosF : Future[List[Row]] = Select.select("select * from person where name='carlitos';").collect[List[Row]]
      val carlitos = Await.result(carlitosF, Duration(15, SECONDS))

      val person = personFormat2(None, carlitos.head)
      println(person)
      person === p1

    }
  }
  
  "Create" should {
    
  }

}
