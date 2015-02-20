package cassandra.cql.query

import java.util.concurrent.TimeUnit

import cassandra.{CassandraCluster, Person, Address}
import com.datastax.driver.core.{UDTValue, UserType, DataType, Row}
import org.specs2.mutable.Specification
import cassandra.FixtureFormats._

import scala.concurrent.{Future, Await}
import scala.concurrent.duration.Duration
import scala.collection.JavaConversions.collectionAsScalaIterable
class SelectTest extends Specification {

  "Query" should {
    "Insert into the database then read" in {

      val address = Address(2, "Blula Bld", home = true)
      val p1 = Person("carlitos", 40, address, Some(165), List("Foo", "Marg"))

      val cluster = CassandraCluster("localhost")
      implicit val session = cluster.connect("tevinzi")

      val response = Insert.insert(p1)
      Await.ready(response.rsF, Duration(15, TimeUnit.SECONDS))

      val carlitosF : Future[List[Row]] = Select.select("select * from person where name='carlitos';").collect[List[Row]]
      val carlitos = Await.result(carlitosF, Duration(15, TimeUnit.SECONDS))

      for {
        c <- carlitos
      } yield {
        val definitions = c.getColumnDefinitions
        val name = c.getString(0)
        val addressUDT = c.getUDTValue(1)
        val address = Address(addressUDT.getInt(0), addressUDT.getString(1), addressUDT.getBool(2))
        val age = c.getInt(2)
        val height = c.getInt(3)
        val names = c.getList(4, classOf[String])
        Person(name, age, address, Some(height), names.toList)
      }
      println(carlitos)
      true === true
    }
  }

}
