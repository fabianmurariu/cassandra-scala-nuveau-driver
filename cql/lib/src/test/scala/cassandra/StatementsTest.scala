package cassandra

import cassandra.Cql._
import cassandra.Statements._
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
      column.insert === s"INSERT into random ${column.fields} VALUES ${column.values}"
    }
  }

  "CqlTable.createTable" should {
    "list all the column types" in {
      val column = CqlTable(CqlType("random",
        "name" -> CqlText("john doe"),
        "age" -> CqlNumber(23)))
      column.createTable === "CREATE table random (" +
        "name text," +
        "age int" +
        ")"
    }

  }

  "CqlTable.createType" should {
    "list all the column types" in {
      val column = CqlTable(CqlType("random",
        "name" -> CqlText("john doe"),
        "age" -> CqlNumber(23)))
      column.createType === "CREATE table random (" +
        "name text," +
        "age int" +
        ")"
    }

  }

}
