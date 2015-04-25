package cassandra.handlers.json

import java.util.concurrent.TimeUnit

import cassandra.CassandraCluster
import cassandra.cql.query.BasicSelect
import com.datastax.driver.core.Row
import org.specs2.mutable.Specification
import play.api.libs.json.Json

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class Cql2JsonReaderSpec extends Specification {

  "Cql2JsonReader" should {

    "transform Row => JsValue with lists no tuples" >> {

      val cluster = CassandraCluster("localhost")
      implicit val session = cluster.connect("tevinzi")

      val rowsF = BasicSelect.select("select * from PERSON").collect[List[Row]]

      val rows = Await.result(rowsF, Duration(15, TimeUnit.SECONDS))

      Cql2JsonReader.read(rows.head) === Json.obj(
        "name" -> "john doe",
        "age" -> 23,
        "birthdate" -> 954756930555L,
        "address" -> Json.obj(
          "house" -> 12,
          "street" -> "Lula Str",
          "home" -> true
        ),
        "height" -> 165,
        "othernames" -> Json.arr("Jack", "Black")
      )
    }
  }

}
