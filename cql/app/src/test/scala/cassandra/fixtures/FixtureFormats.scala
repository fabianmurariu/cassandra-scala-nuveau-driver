package cassandra.fixtures

import cassandra.format.{CqlFormat, DataTypeFormat}
import cassandra.implicits._
import cassandra.{CqlHandler, CqlTypeHandler}
import cassandra.cql.CqlType
import cassandra.cql.UserDefineDt
import scala.language.implicitConversions

trait FixtureFormats {
  implicit def toCqlHandler[T](t: T): CqlHandler[T] = new CqlHandler[T](t)

  implicit def toCqlDataTypeHandler[T](t: Class[T]): CqlTypeHandler[T] = new CqlTypeHandler[T](t)
}

trait CqlValueFormats extends CqlImplicits {
  implicit val addressCqlValueFormat: CqlFormat[Address] = cqlFormat[Address]
  implicit val personCqlValueFormat: CqlFormat[Person] = cqlFormat[Person]

}

trait CqlDataTypeFormats extends CqlDataTypeImplicits{
  implicit val addressCqlDataTypeFormat: DataTypeFormat[Address] = cqlDataTypeFormat[Address]
  implicit val personCqlDataTypeFormat: DataTypeFormat[Person] = cqlDataTypeFormat[Person]

}


object FixtureFormats extends FixtureFormats with CqlValueFormats with CqlDataTypeFormats with ExtraImplicits

object CqlValueFormats extends CqlValueFormats

object CqlDataTypeFormats extends CqlDataTypeFormats

