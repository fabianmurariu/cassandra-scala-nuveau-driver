package cassandra.cql.query

import java.util.concurrent.TimeUnit

import cassandra.CqlReaderFormats._
import cassandra.FixtureFormats._
import cassandra.{Address, CassandraCluster, Person}
import com.datastax.driver.core.Row
import org.specs2.mutable.Specification

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
class SelectTest extends Specification {

  "Query" should {
    "Insert into the database then read with macro formats" in {

      val address = Address(2, "Blula Bld", home = true)
      val p1 = Person("carlitos", 40, address, Some(165), List("Foo", "Marg"))

      val cluster = CassandraCluster("localhost")
      implicit val session = cluster.connect("tevinzi")

      val response = Insert.insert(p1)
      Await.ready(response.rsF, Duration(15, TimeUnit.SECONDS))

      val carlitosF : Future[List[Row]] = Select.select("select * from person where name='carlitos';").collect[List[Row]]
      val carlitos = Await.result(carlitosF, Duration(15, TimeUnit.SECONDS))

      val person = personFormat2(None, carlitos.head)
      println(person)
      person === p1

    }
  }

}
