package cassandra.implicits

import cassandra.format.DataTypeFormat
import cassandra.cql._
import scala.language.implicitConversions

trait ExtraImplicits {
  self: CqlDataTypeImplicits =>

  type DTF[T] = DataTypeFormat[T]
  implicit def tuple2Format[T1: DTF, T2: DTF]: DTF[(T1, T2)] = cqlTupleTypeFormat[(T1, T2)]
  implicit def tuple3Format[T1: DTF, T2: DTF, T3: DTF]: DTF[(T1, T2, T3)] = cqlTupleTypeFormat[(T1, T2, T3)]
  implicit def tuple4Format[T1: DTF, T2: DTF, T3: DTF, T4: DTF]: DTF[(T1, T2, T3, T4)] = cqlTupleTypeFormat[(T1, T2, T3, T4)]
  implicit def tuple5Format[T1: DTF, T2: DTF, T3: DTF, T4: DTF, T5: DTF]: DTF[(T1, T2, T3, T4, T5)] = cqlTupleTypeFormat[(T1, T2, T3, T4, T5)]
  implicit def tuple6Format[T1: DTF, T2: DTF, T3: DTF, T4: DTF, T5: DTF, T6: DTF]: DTF[(T1, T2, T3, T4, T5, T6)] = cqlTupleTypeFormat[(T1, T2, T3, T4, T5, T6)]
  implicit def tuple7Format[T1: DTF, T2: DTF, T3: DTF, T4: DTF, T5: DTF, T6: DTF, T7: DTF]: DTF[(T1, T2, T3, T4, T5, T6, T7)] = cqlTupleTypeFormat[(T1, T2, T3, T4, T5, T6, T7)]

}
