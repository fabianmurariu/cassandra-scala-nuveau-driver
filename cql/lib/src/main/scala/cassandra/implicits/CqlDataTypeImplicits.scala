package cassandra.implicits

import cassandra.cql._
import cassandra.format.{CqlFormat, DataTypeFormat}
import cassandra.CqlMacros

import scala.language.implicitConversions

trait CqlDataTypeLowPriorityImplicits {
  implicit def cqlDataTypeFormat[A]: DataTypeFormat[A] = macro CqlMacros.cqlDataTypeFormatMacro[A]
}

trait CqlDataTypeImplicits extends CqlDataTypeLowPriorityImplicits {

  implicit val intDataTypeFormat: DataTypeFormat[Int] = makeDataTypeFormat { () => IntDt}
  implicit val booleanDataTypeFormat: DataTypeFormat[Boolean] = makeDataTypeFormat { () => BooleanDt}
  implicit val stringDataTypeFormat: DataTypeFormat[String] = makeDataTypeFormat { () => TextDt}

  implicit def optionsFormat[T: DataTypeFormat]: OptionFormat[T] = new OptionFormat[T]()

  implicit def listFormat[T: DataTypeFormat]: ListFormat[T] = new ListFormat[T]()

  class ListFormat[T: DataTypeFormat] extends DataTypeFormat[List[T]] {
    override def apply(): CqlDataType = {
      val format = implicitly[DataTypeFormat[T]]
      ListDt(format())
    }
  }

  class OptionFormat[T: DataTypeFormat] extends DataTypeFormat[Option[T]] {
    override def apply(): CqlDataType = {
      val format = implicitly[DataTypeFormat[T]]
      format()
    }
  }

  def makeDataTypeFormat[T](f: () => CqlDataType) = new DataTypeFormat[T] {
    override def apply(): CqlDataType = f()
  }

}

object CqlDataTypeImplicits extends CqlDataTypeImplicits {
  def toUserDefinedType[T: DataTypeFormat](value: T): CqlDataType = {
    val format = implicitly[DataTypeFormat[T]]
    format()
  }
}
