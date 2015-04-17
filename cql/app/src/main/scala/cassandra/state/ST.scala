package cassandra.state

case class World[S]()

case class ST[S, R](f:World[S] => (World[S], R)) extends (World[S] => (World[S], R)){
  override def apply(v1: World[S]): (World[S], R) = f(v1)

  def flatMap[R2](g : R => ST[S, R2]): ST[S, R2] = ST(s => {
    val (nextState, a: R) = f(s)
    g(a)(nextState)
  })

  def map[R2](g: R => R2): ST[S, R2] =
    ST(s => {
      val (nextState, a) = f(s)
      (nextState, g(a))
    })
}

object ST{
  def returnST[S, A](a: => A): ST[S, A] = ST(s => (s, a))
}

case class STRef[S, A](a: A) {
  private var value: A = a

  def read: ST[S, A] = ST.returnST(value)
  def write(a: A): ST[S, STRef[S, A]] = ST((s: World[S]) => {value = a; (s, this)})
  def mod[B](f: A => A): ST[S, STRef[S, A]] = for {
    a <- read
    v <- write(f(a))
  } yield v
}
