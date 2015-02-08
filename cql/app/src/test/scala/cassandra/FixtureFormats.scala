package cassandra

import cassandra.format.{CqlFormat, DataTypeFormat}
import cassandra.implicits.{CqlDataTypeImplicits, CqlImplicits}

import scala.language.implicitConversions

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

object FixtureFormats extends FixtureFormats with CqlValueFormats with CqlDataTypeFormats

object CqlValueFormats extends CqlValueFormats

object CqlDataTypeFormats extends CqlDataTypeFormats