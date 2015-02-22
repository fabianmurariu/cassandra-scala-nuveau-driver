package cassandra.fixtures

import cassandra.cql._
import cassandra.format.{CqlDataReader, CqlFormat, DataTypeFormat}
import cassandra.implicits._
import cassandra.{CqlHandler, CqlTypeHandler}
import com.datastax.driver.core._
import cassandra.CqlMacros
import scala.collection.JavaConversions
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
  implicit val pairCqlValueFormat: CqlFormat[Pair] = cqlFormat[Pair]

}

trait CqlDataTypeFormats extends CqlDataTypeImplicits{
  import cassandra.cql.UserDefineDt
  implicit val addressCqlDataTypeFormat: DataTypeFormat[Address] = cqlDataTypeFormat[Address]
  implicit val personCqlDataTypeFormat: DataTypeFormat[Person] = cqlDataTypeFormat[Person]
  implicit val pairCqlDataTypeFormat: DataTypeFormat[Pair] = cqlDataTypeFormat[Pair]

}

trait CqlReaderImplicits extends CqlReaderFormats with LowPriorityCqlReaderFormats {
  implicit val addressFormat2: CqlDataReader[Address] = cqlReaderFormat[Address]
  implicit val personFormat2: CqlDataReader[Person] = cqlReaderFormat[Person]
//  implicit val pairFormat2: CqlDataReader[Pair] = cqlReaderFormat[Pair]
}

object FixtureFormats extends FixtureFormats with CqlValueFormats with CqlDataTypeFormats with ExtraImplicits with CqlReaderImplicits

object CqlValueFormats extends CqlValueFormats

object CqlDataTypeFormats extends CqlDataTypeFormats

