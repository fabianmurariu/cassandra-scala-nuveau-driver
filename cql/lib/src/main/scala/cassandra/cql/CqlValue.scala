package cassandra.cql

import org.joda.time.DateTime

trait CqlValue {
  def values: String
}

case class CqlType(name: String, fields: (String, CqlValue)*) extends CqlValue {
  override lazy val values = s"{${
    fields
      .map { case (k, v) => s"$k:${v.values}"}
      .mkString(",")
  }}"
}

case class CqlTuple(fields:CqlValue*) extends CqlValue{
  override lazy val values = s"(${fields.map(_.values).mkString(",")})"
}

case class CqlTable(ofType: CqlType) extends CqlValue {
  override lazy val values = s"(${
    ofType.fields.map(_._2.values).mkString(",")
  })"
}

case class CqlText(value: String) extends CqlValue {
  override lazy val values = s"'$value'"
}

case class CqlDateTime(value:DateTime) extends CqlValue{
  override lazy val values = s"'${value.toString}'"
}

case class CqlNumber[T: Numeric](value: T) extends CqlValue {
  override lazy val values = s"$value"
}

abstract class CqlBoolean(value: Boolean) extends CqlValue {
  override lazy val values = s"$value"
}

case object CqlTrue extends CqlBoolean(true)

case object CqlFalse extends CqlBoolean(false)

case object CqlNull extends CqlValue {
  override lazy val values = "null"
}

case class CqlSet(xs: Set[CqlValue]) extends CqlValue {
  override lazy val values = s"{${xs.map(_.values).mkString(",")}}"
}

case class CqlList(xs: Vector[CqlValue]) extends CqlValue {
  override lazy val values = s"[${xs.map(_.values).mkString(",")}]"
}

object CqlList {
  def apply(xs: CqlValue*) = new CqlList(xs.toVector)
}

object CqlSet {
  def apply(xs: CqlValue*) = new CqlSet(xs.toSet)
}