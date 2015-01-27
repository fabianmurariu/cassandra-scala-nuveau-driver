package cassandra.cql

sealed trait CqlDataType

abstract class BaseDataType(name: String) extends CqlDataType

case class ListDt(subType: CqlDataType) extends BaseDataType("list")

case class SetDt(subType: CqlDataType) extends BaseDataType("set")

case class UserDefineDt(name: String, types: (String, CqlDataType)*) extends BaseDataType(name)

case object TextDt extends BaseDataType("text")

case object IntDt extends BaseDataType("int")

case object LongDt extends BaseDataType("bigint")

case object BooleanDt extends BaseDataType("boolean")

case object DoubleDt extends BaseDataType("double")

case object FloatDt extends BaseDataType("float")

case object DecimalDt extends BaseDataType("decimal")
