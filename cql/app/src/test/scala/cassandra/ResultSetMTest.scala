package cassandra

import java.util.concurrent.TimeUnit

import cassandra.FixtureFormats._
import cassandra.cql.query.GuavaFuturesAdapter
import com.datastax.driver.core._
import org.specs2.mutable.Specification
import play.api.libs.iteratee.{Iteratee, Enumerator}

import scala.collection.generic.CanBuildFrom
import scala.collection.mutable
import scala.concurrent.{duration, Await, Future}
import scala.language.higherKinds
import scala.concurrent.duration.Duration
import scala.concurrent.duration.TimeUnit

class ResultSetMTest extends Specification {

  //  "create type and table" in {
  //    val cluster = CassandraCluster("localhost")
  //    val session = cluster.connect("tevinzi")
  //
  //    session.execute("DROP TABLE if EXISTS person;")
  //    session.execute("DROP TYPE if EXISTS address;")
  //
  //    val address = Address(12, "Lula Str", home = true)
  //    val person = Person("john doe", 23, address, Some(165), List("Jack", "Black"))
  //
  //    val createAddressType = classOf[Address].createType
  //    println(createAddressType)
  //    session.execute(createAddressType)
  //    val createPersonTable = classOf[Person].createTable
  //    println(createPersonTable)
  //    session.execute(createPersonTable)
  //
  //    session.execute(person.insert)
  //
  //    true === true // <- fix this once read is available
  //  }

  "trigger a query with a fetch size" in {

    import GuavaFuturesAdapter._

    val cluster = CassandraCluster("localhost")
    val session = cluster.connect("tevinzi")

    val address = Address(12, "Lula Str", home = true)
    val p1 = Person("black americano", 120, address, Some(165), List("Jack", "Turd"))
    val p2 = Person("sexy jade", 90, address, Some(113), List("Baah", "Mlerg"))
    val p3 = Person("ikea", 2, address, Some(12), List("Jingle", "Baah"))

    session.execute(p1.insert)
    session.execute(p2.insert)
    session.execute(p3.insert)

    val statement = new SimpleStatement("SELECT * from PERSON")
    statement.setFetchSize(1)

    val rsF:Future[ResultSet] = session.executeAsync(statement)

    def nextStream(rs:ResultSet):Future[Stream[Row]] = for {
      _ <- rs.fetchMoreResults()
    } yield grabUntilNextPageOrExhausted(rs)

    val bRows: Enumerator[Stream[Row]] = Enumerator.unfoldM(rsF) {
      rsF => for {
        rs <- rsF
        rows <- nextStream(rs)
      } yield {
        if (rs.isExhausted && rows.isEmpty) None
        else Some((rsF, rows))
      }
    }


    val result: Future[Stream[Row]] = bRows |>>> Iteratee.fold(Stream.empty[Row])((r, e:Stream[Row]) => r ++ e)

    val rows = Await.result(result, Duration(15, TimeUnit.SECONDS))
    rows.foreach(println)

    true === true
  }

}
