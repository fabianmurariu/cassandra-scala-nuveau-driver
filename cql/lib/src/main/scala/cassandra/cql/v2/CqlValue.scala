package cassandra.cql.v2

trait CqlValue {
  val fields:Seq[String] = Nil
  val values:List[_]
}

case class CqlObject(name:String, columns:(String, CqlValue)*) extends CqlValue{
  override val fields = columns.map(_._1)
  override val values = columns.view.map(_._2).flatten(_.values).toList
}