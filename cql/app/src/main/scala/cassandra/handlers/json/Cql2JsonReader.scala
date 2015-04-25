package cassandra.handlers.json

import java.math.{BigInteger, BigDecimal}
import java.net.InetAddress
import java.nio.ByteBuffer
import java.util
import java.util.{UUID, Date}
import com.datastax.driver.core.DataType.Name
import com.datastax.driver.core.DataType.Name._
import com.datastax.driver.core._
import play.api.data.validation.ValidationError
import play.api.libs.json._

import scala.collection.JavaConversions.iterableAsScalaIterable
import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConversions.mapAsScalaMap

object Cql2JsonReader {

  def read(data: Row): JsValue = {
    val fields: Seq[(String, JsValue)] = for {
      column <- data.getColumnDefinitions.asList()
      name = column.getName
    } yield name -> Cql2JsonReader.toJson(name, CqlTypeRef(column.getType))(data)
    JsObject(fields)
  }

  class GettableDataFromValue(value: Any) extends GettableByNameData {
    def unsafe[T]: T = value.asInstanceOf[T]
    def getUUID(name: String): UUID = unsafe[UUID]
    def getVarint(name: String): BigInteger = unsafe[BigInteger]
    def getTupleValue(name: String): TupleValue = unsafe[TupleValue]
    def getInet(name: String): InetAddress = unsafe[InetAddress]
    def getList[T](name: String, elementsClass: Class[T]): util.List[T] = unsafe[util.List[T]]
    def getDouble(name: String): Double = unsafe[Double]
    def getBytesUnsafe(name: String): ByteBuffer = unsafe[ByteBuffer]
    def getUDTValue(name: String): UDTValue = unsafe[UDTValue]
    def getFloat(name: String): Float = unsafe[Float]
    def getLong(name: String): Long = unsafe[Long]
    def getBool(name: String): Boolean = unsafe[Boolean]
    def getMap[K, V](name: String, keysClass: Class[K], valuesClass: Class[V]): util.Map[K, V] = unsafe[util.Map[K, V]]
    def getDecimal(name: String): java.math.BigDecimal = unsafe[BigDecimal]
    def isNull(name: String): Boolean = value == null
    def getSet[T](name: String, elementsClass: Class[T]): util.Set[T] = unsafe[util.Set[T]]
    def getDate(name: String): Date = unsafe[Date]
    def getInt(name: String): Int = unsafe[Int]
    def getBytes(name: String): ByteBuffer = unsafe[ByteBuffer]
    def getString(name: String): String = unsafe[String]
  }

  private def toJson(name: String, tpe: CqlTypeRef)(data: GettableByNameData): JsValue = {

    if (!tpe.isCollection) {
      tpe.name match {
        case BIGINT | COUNTER => JsNumber(data.getLong(name))
        case INT => JsNumber(data.getInt(name))
        case DOUBLE => JsNumber(data.getDouble(name))
        case DECIMAL => JsNumber(data.getDecimal(name))
        case FLOAT => JsNumber(data.getFloat(name).toDouble)
        case TEXT | VARCHAR | ASCII => JsString(data.getString(name))
        case BOOLEAN => JsBoolean(data.getBool(name))
        case TIMESTAMP => JsNumber(data.getDate(name).getTime)
        case Name.UUID | TIMEUUID => JsString(data.getUUID(name).toString)
        case VARINT => JsNumber(new BigDecimal(data.getVarint(name)))
        case UDT =>
          val udtValue = data.getUDTValue(name)
          val userType = udtValue.getType
          JsObject(userType.getFieldNames.map(name => name -> toJson(name, CqlTypeRef(userType.getFieldType(name)))(udtValue)).toSeq)
      }
    } else tpe match {
      case CqlTypeRef(LIST, true, Some(listTypeRef), None) =>
        val values = data.getList(name, listTypeRef.name.asJavaClass())
          .toList.map(value => toJson(null, listTypeRef)(new GettableDataFromValue(value)))
        JsArray(values)
      case CqlTypeRef(SET, true, Some(listTypeRef), None) =>
        val values = data.getSet(name, listTypeRef.name.asJavaClass())
          .toList.map(value => toJson(null, listTypeRef)(new GettableDataFromValue(value)))
        JsArray(values)
      case CqlTypeRef(MAP, true, None, Some((keyTypeRef, valueTypeRef))) =>
        val values = data.getMap(name, keyTypeRef.name.asJavaClass(), valueTypeRef.name.asJavaClass())
        ???
    }
  }

  case class CqlTypeRef(name: Name, isCollection: Boolean, listType: Option[CqlTypeRef], mapType: Option[(CqlTypeRef, CqlTypeRef)])

  object CqlTypeRef {
    def apply(dt: DataType): CqlTypeRef = if (dt.isCollection) {
      dt.getTypeArguments.toList match {
        case head :: Nil => CqlTypeRef(dt.getName, dt.isCollection, Some(CqlTypeRef(head)), None)
        case keyType :: valueType :: Nil => CqlTypeRef(dt.getName, dt.isCollection, None, Some((CqlTypeRef(keyType), CqlTypeRef(valueType))))
      }
    } else CqlTypeRef(dt.getName, dt.isCollection, None, None)
  }

  case class JsonReadsException(errors:Seq[(JsPath, Seq[ValidationError])]) extends RuntimeException(s"Failed to read JSON : $errors")

}
