package cassandra

import cassandra.format.{CqlDataReader, CqlFormat, DataTypeFormat}
import cassandra.implicits.{CqlDataTypeImplicits, CqlImplicits}
import com.datastax.driver.core.{UDTValue, GettableByNameData}

import scala.collection.JavaConversions
import scala.collection.generic.CanBuildFrom
import scala.language.implicitConversions
import scala.util.Try

trait FixtureFormats {
  implicit def toCqlHandler[T](t:T):CqlHandler[T] = new CqlHandler[T](t)
  implicit def toCqlDataTypeHandler[T](t:Class[T]):CqlTypeHandler[T] = new CqlTypeHandler[T](t)
}

trait CqlValueFormats extends CqlImplicits {
  import cassandra.cql.CqlType
  implicit val addressCqlValueFormat: CqlFormat[Address] = cqlFormat[Address]
  implicit val personCqlValueFormat: CqlFormat[Person] = cqlFormat[Person]

}

trait CqlDataTypeFormats extends CqlDataTypeImplicits {
  import cassandra.cql.UserDefineDt
  implicit val addressCqlDataTypeFormat: DataTypeFormat[Address] = cqlDataTypeFormat[Address]
  implicit val personCqlDataTypeFormat: DataTypeFormat[Person] = cqlDataTypeFormat[Person]

}

trait CqlReaderFormats {

  abstract class UDTCqlDataReader[T] extends CqlDataReader[T, UDTValue]{
    override def cqlClass:Class[UDTValue] = classOf[UDTValue]
  }

  implicit val addressFormat: CqlDataReader[Address, UDTValue] = new UDTCqlDataReader[Address] {
    override def apply(name:Option[String], d: GettableByNameData): Address = {
      val street = d.getString("street")
      val house = d.getInt("house")
      val home = d.getBool("home")
      Address(house, street, home)
    }
  }

  implicit val personFormat: CqlDataReader[Person, UDTValue] = new UDTCqlDataReader[Person] {

    override def apply(name: Option[String], d:GettableByNameData): Person = {
      val origin = getWithFallBack()
      val readerString = implicitly[CqlDataReader[String, String]]
      val readerInt = implicitly[CqlDataReader[Int, Int]]
      val name: String = readerString(Some("name"), d)
      val age: Int = readerInt(Some("age"), d)
      val readerAddress = implicitly[CqlDataReader[Address, UDTValue]]
      val address: Address = readerAddress(Some("address"), d.getUDTValue("address"))
      val height: Option[Int] = Option(d.getInt("height"))
      val otherNames: List[String] = JavaConversions.asScalaBuffer(d.getList("otherNames", classOf[String])).toList
      Person(name, age, address, height, otherNames)
    }

    def getWithFallBack(name:String, d:UDTValue):UDTValue = {
      Try(d.getUDTValue(name)).recover{case _ => d}.get
    }
  }

  implicit def optionFormat[T, M](implicit tFormat:CqlDataReader[T, M]):CqlDataReader[Option[T], M] = new CqlDataReader[Option[T], M] {
    override def apply(name:Option[String], v1: GettableByNameData): Option[T] = Option(tFormat(name, v1))

    override def cqlClass: Class[M] = tFormat.cqlClass
  }

  implicit def iterableFormat[T, M <: TraversableOnce[T]](implicit xFormat:CqlDataReader[T,T],
                                                          canBuildFrom:CanBuildFrom[Iterable[T], T, M]): CqlDataReader[M,T] = {
    new CqlDataReader[M, T] {
      override def cqlClass: Class[T] = xFormat.cqlClass

      override def apply(v1: Option[String], v2: GettableByNameData): M = {
        val values: Iterable[T] = JavaConversions.collectionAsScalaIterable(v2.getList(v1.get, xFormat.cqlClass))
        val builder = canBuildFrom(values)
        builder.result()
      }
    }
  }
}

object FixtureFormats extends FixtureFormats with CqlValueFormats with CqlDataTypeFormats

object CqlValueFormats extends CqlValueFormats

object CqlDataTypeFormats extends CqlDataTypeFormats