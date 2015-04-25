package cassandra.dsl.words

import cassandra.cql.query.{BasicSelect, ResultSetHandler}
import cassandra.dsl.effects.Keyspace
import cassandra.handlers.json.Cql2JsonReader
import cassandra.handlers.json.Cql2JsonReader.JsonReadsException
import com.datastax.driver.core.Row
import play.api.libs.json.Reads

import scala.concurrent.Future
import scala.reflect.ClassTag

case class Select[TABLE](queryLike: QueryLike[TABLE])
                        (implicit ks: Keyspace) extends StatementLike with BasicSelect with ResultSetHandler {

  val SELECT = "SELECT * FROM "

  def one(implicit reads: Reads[TABLE], tag: ClassTag[TABLE]): Future[Option[TABLE]] = {
    val (where, values) = queryLike.ms.prepCql
    val query = s"""$SELECT "${ks.name}"."${tag.runtimeClass.getSimpleName.toLowerCase}" WHERE $where;"""
    println(query)
    val javaValues = values.asInstanceOf[Seq[_ <: AnyRef]]
    implicit val (session, ex) = ks.params

    for {
      rows: List[Row] <- select(session.prepare(query).bind(javaValues: _*)).collect[List[Row]]
      rowO = rows.headOption
    } yield {
      rowO.map(row => {
        reads.reads(Cql2JsonReader.read(row)).asEither match {
          case Right(value) => value
          case Left(errors) => throw new JsonReadsException(errors)
        }
      })
    }
  }

  def range(page: Range)(implicit reads: Reads[TABLE]): Future[List[TABLE]] = ???

  def count: Future[Long] = ???

  def all(implicit reads: Reads[TABLE], tag: ClassTag[TABLE]): Future[List[TABLE]] = ???

  def rows(implicit reads: Reads[TABLE], tag: ClassTag[TABLE]): Future[List[Row]] = ???
}
