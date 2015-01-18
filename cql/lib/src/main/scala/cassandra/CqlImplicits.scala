package cassandra

import cassandra.Cql._

trait LowPriorityImplicits{
  implicit def cqlFormat[A]: CqlFormat[A] = macro CqlMacros.cqlFormatMacro[A]
}

trait CqlImplicits extends LowPriorityImplicits{

  private def makeFormat[T](f:T => CqlValue):CqlFormat[T] = new CqlFormat[T] {
    override def apply(v1: T): CqlValue = f(v1)
  }
  /* Strings */
  implicit val textFormat = makeFormat[String](text => CqlText(text))
  /* booleans*/
  implicit val booleanFormat = makeFormat[Boolean]{
    case true => CqlTrue
    case false => CqlTrue
  }

  /*Lists */

  class ListFormat[T:CqlFormat] extends CqlFormat[List[T]]{
    override def apply(v1: List[T]): CqlValue = {
      val tFormat = implicitly[CqlFormat[T]]
      CqlList(v1.map(tFormat).toVector)
    }
  }

  implicit def seqFormat[T:CqlFormat]: ListFormat[T] = new ListFormat[T]
  /* options*/
  implicit def optionFormat[T: CqlFormat]: OptionFormat[T] = new OptionFormat[T]

  class OptionFormat[T:CqlFormat] extends CqlFormat[Option[T]]{
    override def apply(v1: Option[T]): CqlValue = {
      val format = implicitly[CqlFormat[T]]
      v1 match {
        case Some(v) => format(v)
        case None => CqlNull
      }
    }
  }

  /* Numbers */

  implicit val intFormat = numberFormat[Int]
  implicit val doubleFormat = numberFormat[Double]
  implicit val floatFormat = numberFormat[Float]
  implicit val longFormat = numberFormat[Long]
  implicit val bigDecFormat = numberFormat[BigDecimal]
  implicit val bigIntFormat = numberFormat[BigInt]

  implicit def numberFormat[T:Numeric]: CqlFormat[T] = makeFormat(num => CqlNumber(num))

}

object CqlImplicits extends CqlImplicits{
  def writeCql[T:CqlFormat](value:T):CqlValue = {
    val format: CqlFormat[T] = implicitly[CqlFormat[T]]
    format(value)
  }
}
