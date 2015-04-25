package cassandra.futures

import java.util.concurrent.Executor

import com.datastax.driver.core.{ResultSet, Row}
import com.google.common.util.concurrent.ListenableFuture

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.language.implicitConversions
import scala.util.Try

trait GuavaFuturesAdapter {

  implicit def toExecutor(ex: ExecutionContext): Executor = new Executor {
    override def execute(command: Runnable): Unit = ex.prepare().execute(command)
  }

  implicit def toRunnable(f: () => Unit): Runnable = new Runnable {
    override def run(): Unit = f()
  }

  def toMappedFuture[T, M](glFuture: ListenableFuture[T])(implicit ex: ExecutionContext, map: T => M): Future[M] = {
    val p = Promise[M]()
    glFuture.addListener(() => {
      p.complete(Try(map(glFuture.get())))
      ()
    }, ex.prepare())
    p.future
  }

  implicit def toFuture[T](glFuture: ListenableFuture[T])(implicit ex: ExecutionContext): Future[T] =
    toMappedFuture[T, T](glFuture)(ex, t => t)

  implicit def toResultSetFuture(rsFuture: ListenableFuture[ResultSet])
                                (implicit ex: ExecutionContext): Future[Option[ResultSet]] = {
    def toOption(rs: ResultSet) = if (!rs.isExhausted) Some(rs) else None
    toMappedFuture(rsFuture)(ex.prepare(), toOption)
  }

}

object GuavaFuturesAdapter extends GuavaFuturesAdapter {

  def grabUntilNextPageOrExhausted(rs: ResultSet): Stream[Row] = {
    if (rs.getAvailableWithoutFetching > 0 && !rs.isExhausted)
      rs.one #:: grabUntilNextPageOrExhausted(rs)
    else Stream.empty
  }

}