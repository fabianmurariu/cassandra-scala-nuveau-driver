package cassandra.dsl

import java.time.LocalDateTime._
import java.time.format.DateTimeFormatter._

import cassandra.fixtures.Person
import org.specs2.mutable.Specification
import cassandra.dsl.Dsl._

class DslTest extends Specification {

  "Dsl" should {
    "support select CQL" in {

      val table = new CqlTable[Person] {}

      table.selectFrom[Person] where (_.age in List(1, 3, 4))
      table.selectFrom[Person] where (_.name == "John") and (_.birthDate >= parse("2000-04-03T10:15:30.555Z", ISO_DATE_TIME))
      table.selectFrom[Person] where (_.name == "John") and (_.birthDate >= parse("2000-04-03T10:15:30.555Z", ISO_DATE_TIME)) count

      pending("not yet")
    }
  }

}
