package cassandra.state

import cassandra.fixtures.Person
import org.specs2.mutable.Specification
import State._

class StateSpec extends Specification {

  "state" should {
    "be lazy" in {
      val io = for {
        i <- get[Int]
        _ <- set(i + 3)
        j <- get
        _ <- set(j - 2)
        k <- get
      } yield k

      run(41, io) === 42
    }

  }

}
