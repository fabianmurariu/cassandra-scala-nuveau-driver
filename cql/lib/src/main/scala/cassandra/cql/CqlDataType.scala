package cassandra.cql

sealed trait CqlDataType {
  def name:String
}

abstract class BaseDataType(val name: String) extends CqlDataType

case class ListDt(subType: CqlDataType) extends BaseDataType(s"list<${subType.name}>")
case class SetDt(subType: CqlDataType) extends BaseDataType(s"set<${subType.name}>")
case class TupleDt(subTypes: CqlDataType*) extends BaseDataType(s"frozen <tuple<${subTypes.map(_.name).mkString(",")}>>")
case class UserDefineDt(userDefinedName: String, ids:Traversable[String], types: (String, CqlDataType)*) extends BaseDataType(s"frozen <$userDefinedName>")

case object UuidDt extends BaseDataType("uuid")
case object TextDt extends BaseDataType("text")
case object BlobDt extends BaseDataType("blob")
case object IntDt extends BaseDataType("int")
case object LongDt extends BaseDataType("bigint")
case object BooleanDt extends BaseDataType("boolean")
case object DoubleDt extends BaseDataType("double")
case object FloatDt extends BaseDataType("float")
case object DecimalDt extends BaseDataType("decimal")
case object TimestampDt extends BaseDataType("timestamp")
