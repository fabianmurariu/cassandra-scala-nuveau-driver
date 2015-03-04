package cassandra.implicits

import cassandra.cql.CqlTuple
import cassandra.format.CqlFormat

import scala.language.implicitConversions

trait TupleImplicits {
  self: LowPriorityImplicits =>

  type CF[T] = CqlFormat[T]

  implicit def tuple1[T1](implicit tf: CF[T1]): CF[(T1)] = makeFormat(t1 => CqlTuple(tf(t1)))

  implicit def tuple2[T1, T2](implicit tf1: CF[T1], tf2: CF[T2]): CF[(T1, T2)] =
    makeFormat(t => CqlTuple(tf1(t._1), tf2(t._2)))

  implicit def tuple3[T1, T2, T3](implicit tf1: CF[T1], tf2: CF[T2], tf3: CF[T3]): CF[(T1, T2, T3)] =
    makeFormat(t => CqlTuple(tf1(t._1), tf2(t._2), tf3(t._3)))

  implicit def tuple4[T1, T2, T3, T4](implicit tf1: CF[T1], tf2: CF[T2], tf3: CF[T3], tf4: CF[T4]): CF[(T1, T2, T3, T4)] =
    makeFormat(t => CqlTuple(tf1(t._1), tf2(t._2), tf3(t._3), tf4(t._4)))

  implicit def tuple5[T1, T2, T3, T4, T5](implicit tf1: CF[T1], tf2: CF[T2], tf3: CF[T3], tf4: CF[T4], tf5: CF[T5]): CF[(T1, T2, T3, T4, T5)] =
    makeFormat(t => CqlTuple(tf1(t._1), tf2(t._2), tf3(t._3), tf4(t._4), tf5(t._5)))

  implicit def tuple6[T1, T2, T3, T4, T5, T6](implicit tf1: CF[T1], tf2: CF[T2], tf3: CF[T3], tf4: CF[T4], tf5: CF[T5], tf6: CF[T6]): CF[(T1, T2, T3, T4, T5, T6)] =
    makeFormat(t => CqlTuple(tf1(t._1), tf2(t._2), tf3(t._3), tf4(t._4), tf5(t._5), tf6(t._6)))

  /* for more add your own */
}
