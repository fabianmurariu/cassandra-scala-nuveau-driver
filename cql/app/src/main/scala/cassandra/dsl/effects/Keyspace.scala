package cassandra.dsl.effects

//import cassandra.dsl.{DeleteLike, InsertLike, UpdateLike}

import cassandra.CassandraCluster
import cassandra.dsl.words.SelectLike
import com.datastax.driver.core.Session

import scala.concurrent.ExecutionContext

trait Keyspace extends SelectLike /*with InsertLike with UpdateLike with DeleteLike*/{

  def cluster:CassandraCluster
  def executionContext:ExecutionContext
  def session:Session
  def name:String
  def params:(Session, ExecutionContext) = (session, executionContext)
}

object Keyspace {

  def apply(ksName:String, cassandraCluster: CassandraCluster) = new Keyspace {
    override def cluster: CassandraCluster = cassandraCluster

    override def executionContext: ExecutionContext = scala.concurrent.ExecutionContext.global

    val session:Session = cassandraCluster.connect(ksName)

    def name = ksName

  }

}
