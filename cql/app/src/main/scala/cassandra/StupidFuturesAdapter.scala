package cassandra

import java.util.concurrent.Executor

import com.datastax.driver.core.ResultSet
import com.google.common.util.concurrent.ListenableFuture

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.language.implicitConversions
import scala.util.Try

trait StupidFuturesAdapter {

  implicit def toExecutor(ex: ExecutionContext): Executor = new Executor {
    override def execute(command: Runnable): Unit = ex.execute(command)
  }

  implicit def toRunnable(f: () => Unit): Runnable = new Runnable {
    override def run(): Unit = f()
  }

  def toMappedFuture[T, M](stupidFuture: ListenableFuture[T])(implicit ex: ExecutionContext, map: T => M): Future[M] = {
    val p = Promise[M]()
    stupidFuture.addListener(() => {
      p.complete(Try(map(stupidFuture.get())))
      ()
    }, ex.prepare())
    p.future
  }

  implicit def toFuture[T](stupidFuture: ListenableFuture[T])(implicit ex: ExecutionContext): Future[T] =
    toMappedFuture[T, T](stupidFuture)(ex, t => t)

  implicit def toResultSetFuture(rsFuture: ListenableFuture[ResultSet])
                                (implicit ex: ExecutionContext): Future[Option[ResultSet]] = {
    def toOption(rs: ResultSet) = if (!rs.isExhausted) Some(rs) else None
    toMappedFuture(rsFuture)(ex.prepare(), toOption)
  }

}

object StupidFuturesAdapter extends StupidFuturesAdapter