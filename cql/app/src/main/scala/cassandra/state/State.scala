package cassandra.state

trait State[S, R] extends (S => (S, R)) { self =>

  def flatMap[R2](f: R => State[S, R2]): State[S, R2] = new State[S, R2] {
    override def apply(s: S): (S, R2) = {
      val (s2, r) = self.apply(s)
      f(r)(s2)
    }
  }

  def map[R2](f: R => R2): State[S, R2] = flatMap[R2](r => State(f(r)))

}

object State {
  def apply[S, R](v:R):State[S, R] = new State[S, R] {
    override def apply(s: S): (S, R) = (s, v)
  }

  def get[S]: State[S, S] = new State[S, S] {
    override def apply(s: S): (S, S) = (s, s)
  }

  def set[S](v: S): State[S, Unit] = new State[S, Unit] {
    override def apply(s: S): (S, Unit) = (v, ())
  }

  def run[S, R](s : S, st: State[S, R]):R = st(s)._2
}


