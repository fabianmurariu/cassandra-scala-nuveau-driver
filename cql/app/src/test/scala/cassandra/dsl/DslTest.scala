package cassandra.dsl

import java.time.LocalDateTime
import java.time.LocalDateTime._
import java.time.format.DateTimeFormatter._

import cassandra.CassandraCluster
import cassandra.dsl.effects.Keyspace
import cassandra.fixtures.Person
import cassandra.query._
import org.specs2.mutable.Specification
import cassandra.dsl.Dsl._

class DslTest extends Specification {

  "Dsl" should {
    "support select CQL" >> {

      val keyspace = Keyspace("tevinzi", CassandraCluster("localhost"))

      def provider:String = "John"

      val simpleSelect = keyspace.selectFrom[Person] {_.where(p=> p.name == "john" && p.age <= 3)}
      simpleSelect === Select(QueryLike(And(Eq("name", "john"), LtEq("age", 3))))

      val inClauseSelect = keyspace.selectFrom[Person] {_.where (_.age in List(1, 3, 4))}
      inClauseSelect === Select(QueryLike(In("age", List(1, 3, 4))))

      def date(d:String) = parse(d, ISO_DATE_TIME)

      val twoClauseSelectWithDef = keyspace.selectFrom[Person] {_.where (p => p.name == "John" && p.birthDate >= date("2000-04-03T10:15:30.555Z"))}
      twoClauseSelectWithDef === Select(QueryLike(And(Eq("name", "John"), GtEq("birthDate", date("2000-04-03T10:15:30.555Z")))))
    }
  }

}
