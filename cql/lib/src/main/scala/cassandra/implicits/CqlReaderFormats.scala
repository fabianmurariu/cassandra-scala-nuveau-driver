package cassandra.implicits

import cassandra.format.CqlDataReader
import com.datastax.driver.core.{GettableByNameData, UDTValue}

import scala.collection.JavaConversions
import scala.language.implicitConversions
import scala.util.Try
import cassandra.CqlMacros

trait LowPriorityCqlReaderFormats {
  def cqlReaderFormat[T]: CqlDataReader[T] = macro CqlMacros.cqlRowReaderMacro[T]
}

trait CqlReaderFormats extends LowPriorityCqlReaderFormats{

  abstract class UDTCqlDataReader[T] extends CqlDataReader[T] {
    override def cqlClass = classOf[UDTValue]

    def getWithFallBack(nameO: Option[String], d: GettableByNameData): GettableByNameData = nameO match {
      case Some(name) => Try(d.getUDTValue(name)).recover { case _ => d}.get
      case None => d
    }
  }

  implicit val stringReaderFormat = new CqlDataReader[String] {
    override def cqlClass: Class[_] = classOf[String]

    override def apply(v1: Option[String], v2: GettableByNameData): String = v2.getString(v1.get)
  }

  implicit val booleanReaderFormat = new CqlDataReader[Boolean] {
    override def cqlClass: Class[_] = classOf[Boolean]

    override def apply(v1: Option[String], v2: GettableByNameData): Boolean = v2.getBool(v1.get)
  }

  implicit val intReaderFormat = new CqlDataReader[Int] {
    override def cqlClass: Class[_] = classOf[Int]

    override def apply(v1: Option[String], v2: GettableByNameData): Int = v2.getInt(v1.get)
  }

  implicit def optionReaderFormat[T](implicit tFormat: CqlDataReader[T]): CqlDataReader[Option[T]] = new CqlDataReader[Option[T]] {
    override def apply(name: Option[String], v1: GettableByNameData): Option[T] = Option(tFormat(name, v1))

    override def cqlClass = tFormat.cqlClass
  }

  //TODO: this does not work well
  implicit def listReaderFormat[T](implicit tFormat: CqlDataReader[T]): CqlDataReader[List[T]] = new CqlDataReader[List[T]] {
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

//object CqlReaderFormats extends CqlReaderFormats with LowPriorityCqlReaderFormats {
//  implicit val addressFormat2: CqlDataReader[Address] = cqlReaderFormat[Address]
//  implicit val personFormat2: CqlDataReader[Person] = cqlReaderFormat[Person]
//  implicit val pairFormat2: CqlDataReader[Pair] = cqlReaderFormat[Pair]
//}