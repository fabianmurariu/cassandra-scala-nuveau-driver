package cassandra.dsl

import java.time.LocalDateTime._
import java.time.format.DateTimeFormatter._

import cassandra.CassandraCluster
import cassandra.dsl.words._
import Dsl._
import cassandra.dsl.effects.Keyspace
import cassandra.fixtures.Person
import org.specs2.mutable.Specification

class DslSpec extends Specification {

  "Dsl" should {
    "support select CQL" >> {

      implicit val ks = Keyspace("tevinzi", CassandraCluster("localhost"))

      def provider:String = "John"

      val simpleSelect = ks.selectFrom[Person] {_.where(p=> p.name == "john" && p.age <= 3)}
      simpleSelect === Select(QueryLike(And(Eq("name", "john"), LtEq("age", 3))))

      val inClauseSelect = ks.selectFrom[Person] {_.where (_.age in List(1, 3, 4))}
      inClauseSelect === Select(QueryLike(In("age", List(1, 3, 4))))

      def date(d:String) = parse(d, ISO_DATE_TIME)

      val twoClauseSelectWithDef = ks.selectFrom[Person] {_.where (p => p.name == "John" && p.birthdate >= date("2000-04-03T10:15:30.555Z"))}
      twoClauseSelectWithDef === Select(QueryLike(And(Eq("name", "John"), GtEq("birthdate", date("2000-04-03T10:15:30.555Z")))))
    }
  }

}
