package cassandra

import cassandra.format.{CqlDataReader, CqlFormat, DataTypeFormat}
import cassandra.implicits.{CqlDataTypeImplicits, CqlImplicits}
import com.datastax.driver.core._
import com.datastax.driver.core.schemabuilder.UDTType

import scala.collection.JavaConversions
import scala.collection.generic.CanBuildFrom
import scala.language.implicitConversions
import scala.util.Try

trait FixtureFormats {
  implicit def toCqlHandler[T](t: T): CqlHandler[T] = new CqlHandler[T](t)

  implicit def toCqlDataTypeHandler[T](t: Class[T]): CqlTypeHandler[T] = new CqlTypeHandler[T](t)
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

trait LowPriorityCqlReaderFormats {
  def cqlReaderFormat[T]: CqlDataReader[T] = macro CqlMacros.cqlRowReaderMacro[T]
}

trait CqlReaderFormats {

  abstract class UDTCqlDataReader[T] extends CqlDataReader[T] {
    override def cqlClass = classOf[UDTValue]

    def getWithFallBack(nameO: Option[String], d: GettableByNameData): GettableByNameData = nameO match {
      case Some(name) => Try(d.getUDTValue(name)).recover { case _ => d}.get
      case None => d
    }
  }

  implicit val stringFormat = new CqlDataReader[String] {
    override def cqlClass: Class[_] = classOf[String]

    override def apply(v1: Option[String], v2: GettableByNameData): String = v2.getString(v1.get)
  }

  implicit val booleanFormat = new CqlDataReader[Boolean] {
    override def cqlClass: Class[_] = classOf[Boolean]

    override def apply(v1: Option[String], v2: GettableByNameData): Boolean = v2.getBool(v1.get)
  }

  implicit val intFormat = new CqlDataReader[Int] {
    override def cqlClass: Class[_] = classOf[Int]

    override def apply(v1: Option[String], v2: GettableByNameData): Int = v2.getInt(v1.get)
  }

  implicit val addressFormat: CqlDataReader[Address] = new UDTCqlDataReader[Address] {
    override def apply(name: Option[String], d: GettableByNameData): Address = {
      val street = d.getString("street")
      val house = d.getInt("house")
      val home = d.getBool("home")
      Address(house, street, home)
    }
  }

  implicit val personFormat: CqlDataReader[Person] = new UDTCqlDataReader[Person] {

    override def apply(nameO: Option[String], d: GettableByNameData): Person = {
      val origin = getWithFallBack(nameO, d)
      val readerString = implicitly[CqlDataReader[String]]
      val readerInt = implicitly[CqlDataReader[Int]]
      val name: String = readerString(Some("name"), origin)
      val age: Int = readerInt(Some("age"), origin)
      val readerAddress = implicitly[CqlDataReader[Address]]
      val address: Address = readerAddress(Some("address"), origin.getUDTValue("address"))
      val height: Option[Int] = Option(d.getInt("height"))
      val otherNames: List[String] = JavaConversions.asScalaBuffer(origin.getList("otherNames", classOf[String])).toList
      Person(name, age, address, height, otherNames)
    }
  }

  implicit def optionFormat[T](implicit tFormat: CqlDataReader[T]): CqlDataReader[Option[T]] = new CqlDataReader[Option[T]] {
    override def apply(name: Option[String], v1: GettableByNameData): Option[T] = Option(tFormat(name, v1))

    override def cqlClass = tFormat.cqlClass
  }

  //TODO: this does not work well
  implicit def listFormat[T](implicit tFormat: CqlDataReader[T]): CqlDataReader[List[T]] = new CqlDataReader[List[T]] {
    override def cqlClass: Class[_] = tFormat.cqlClass

    override def apply(v1: Option[String], v2: GettableByNameData): List[T] = {
      val values: Iterable[Any] = JavaConversions.collectionAsScalaIterable(v2.getList(v1.get, tFormat.cqlClass))
      if (values.isEmpty) Nil
      else {
        values.head match {
          case udt: UDTValue => values.asInstanceOf[Iterable[UDTValue]].map(tFormat(None, _)).toList
          case _ => values.toList.asInstanceOf[List[T]]
        }
      }
    }
  }
}

object FixtureFormats extends FixtureFormats with CqlValueFormats with CqlDataTypeFormats

object CqlValueFormats extends CqlValueFormats

object CqlDataTypeFormats extends CqlDataTypeFormats

object CqlReaderFormats extends CqlReaderFormats with LowPriorityCqlReaderFormats {
  implicit val addressFormat2: CqlDataReader[Address] = cqlReaderFormat[Address]
  implicit val personFormat2: CqlDataReader[Person] = cqlReaderFormat[Person]
}