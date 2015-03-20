package cassandra.statements

import cassandra.cql._
import cassandra.statements.Statements._
import org.joda.time.DateTime
import org.joda.time.DateTimeZone._
import org.specs2.mutable._

class StatementsTest extends Specification {

  "CqlInsert" should {
    "list all the fields " in {
      val column = CqlTable(CqlType("random",
        "name" -> CqlText("john doe"),
        "age" -> CqlNumber(23),
        "address" ->
          CqlType("address",
            "house" -> CqlNumber(12),
            "street" -> CqlText("Lula Str"),
            "home" -> CqlTrue
          ),
        "height" -> CqlNumber(165),
        "otherNames" -> CqlList(CqlText("Jack"), CqlText("Black"))))
      column.fields === "(name,age,address,height,otherNames)"
      column.insert === s"INSERT into random ${column.fields} VALUES ${column.values};"
    }

    "insert dates" in {
      val column = CqlTable(CqlType("blerg",
        "mdate" -> CqlDateTime(new DateTime(2007, 4, 3, 10, 15, 30, 555, UTC))))
      column.insert === s"INSERT into blerg ${column.fields} VALUES ('2007-04-03T10:15:30.555Z');"
    }
  }

  "CqlTable.createTable and createType" should {
    "list all the column types" in {
      val cqlTypeDefinition = UserDefineDt("random", Nil, "firstname" -> TextDt, "lastname" -> TextDt)
      cqlTypeDefinition.createTable === "CREATE TABLE random (" +
        "firstname text," +
        "lastname text" +
        ");"
      cqlTypeDefinition.createType === "CREATE TYPE random (" +
        "firstname text," +
        "lastname text" +
        ");"
    }

  }

  "CqlTable.createTable" should {
    "list all the column types" in {
      val cqlTypeDefinition = UserDefineDt("random", List("id", "name"),
        "id" -> UuidDt,
        "name" -> UserDefineDt("fullname", Nil),
        "direct_reports" -> ListDt(UserDefineDt("fullname", Nil))
      )
      cqlTypeDefinition.createTable === "CREATE TABLE random (" +
        "id uuid," +
        "name frozen <fullname>," +
        "direct_reports list<frozen <fullname>>," +
        "PRIMARY KEY (id,name)" +
        ");"
    }

  }

}
