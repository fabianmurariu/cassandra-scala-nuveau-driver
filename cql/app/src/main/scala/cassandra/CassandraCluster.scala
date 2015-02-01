package cassandra

import com.datastax.driver.core.{Session, Cluster}

case class CassandraCluster(nodes: String*) extends AutoCloseable {
  lazy val cluster = Cluster.builder().addContactPoints(nodes: _*).build()

  override def close(): Unit = cluster.close()

  def connect: Session = cluster.connect()

  def connect(keySpace: String): Session = cluster.connect(keySpace)
}
