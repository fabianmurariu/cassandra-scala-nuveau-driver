package cassandra.implicits

import java.time.{ZoneId, LocalDateTime}
import java.util.Date

import cassandra.format.CqlDataReader
import com.datastax.driver.core.{GettableByNameData, UDTValue}
import org.joda.time.DateTime

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

  implicit val localDateTimeReader:CqlDataReader[LocalDateTime] = new CqlDataReader[LocalDateTime] {
    override def cqlClass: Class[_] = classOf[Date]

    override def apply(v1: Option[String], v2: GettableByNameData): LocalDateTime = {
      val dt = v2.getDate(v1.get)
      LocalDateTime.ofInstant(dt.toInstant, ZoneId.systemDefault())
    }
  }

  implicit val dateTimeReader:CqlDataReader[DateTime] = new CqlDataReader[DateTime] {
    override def cqlClass: Class[_] = classOf[Date]

    override def apply(v1: Option[String], v2: GettableByNameData): DateTime = {
      val dt = v2.getDate(v1.get)
      new DateTime(dt)
    }
  }

  implicit val stringReaderFormat: CqlDataReader[String] = new CqlDataReader[String] {
    override def cqlClass: Class[_] = classOf[String]

    override def apply(v1: Option[String], v2: GettableByNameData): String = v2.getString(v1.get)
  }

  implicit val booleanReaderFormat:CqlDataReader[Boolean] = new CqlDataReader[Boolean] {
    override def cqlClass: Class[_] = classOf[Boolean]

    override def apply(v1: Option[String], v2: GettableByNameData): Boolean = v2.getBool(v1.get)
  }

  implicit val intReaderFormat:CqlDataReader[Int] = new CqlDataReader[Int] {
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
            // TODO: failures all over the place
          case _ => values.toList.asInstanceOf[List[T]]
        }
      }
    }
  }
}