import akka.actor.{ ActorRef, ActorSystem, Props, Actor, Cancellable}
import concurrent.duration._
import language.implicitConversions

class Train(world: ActorRef, horizontalSpeed: Degree, verticalSpeed: Degree) extends Actor {
  import scala.concurrent.ExecutionContext.Implicits.global
  var cancellable: Option[Cancellable] = None
  def receive = {
    case Start => 
      cancellable = Some(context.system.scheduler.schedule(
        0.seconds, 0.5.second, world, RotateBy(Rotation(horizontalSpeed, verticalSpeed))))
    case Stop => cancellable match {
      case Some(c) => c.cancel()
      case None =>
    }
  }
}

class World extends Actor {
  var currentRotation = Rotation(0, 0)
  val printer = context.actorOf(Props[Printer], name = "printer")
  def receive = {
    case RotateBy(r) => 
      currentRotation += r
      printer ! currentRotation
  }
}

object HelloTrains extends App {

  val universe = ActorSystem("universe")
  val world = universe.actorOf(Props[World], name = "world")
  val train = universe.actorOf(Props(new Train(world, 0.01, 0.005)), name = "train")

  train ! Start

  sys.addShutdownHook(universe.shutdown())
}

class Printer extends Actor {
  import scala.Console

  val snap1 = (r: Rotation) => s"""
        ooOOO            Pociąg obraca ziemię pod sobą,
       oo      _____     samemu pozostając w bezruchu.
      _I__n_n__||_||     Aktualny obrót ziemi to: ${r.h}N ${r.v}W
    >(_________|_7_|     
     /o ()() ()() o 
≠≠≠≠≠≠≠≠≠≠≠≠≠≠≠≠≠≠≠≠≠≠≠
·     ·     ·     ·        
  """

  val snap2 = (r: Rotation) => s"""
        ooooo            Pociąg obraca ziemię pod sobą,
       OO      _____     samemu pozostając w bezruchu.
      _I__n_n__||_||     Aktualny obrót ziemi to: ${r.h}N ${r.v}W
    >(_________|_7_|
     /O ()() ()() o 
≠≠≠≠≠≠≠≠≠≠≠≠≠≠≠≠≠≠≠≠≠≠≠
  ·     ·     ·     ·   
  """

  val snap3 = (r: Rotation) => s"""
        OOOoo            Pociąg obraca ziemię pod sobą,
       oo      _____     samemu pozostając w bezruchu.
      _I__n_n__||_||     Aktualny obrót ziemi to: ${r.h}N ${r.v}W
    >(_________|_7_|
     /o ()() ()() O 
≠≠≠≠≠≠≠≠≠≠≠≠≠≠≠≠≠≠≠≠≠≠≠
    ·     ·     ·     ·   
  """

  val it = Iterator.continually(List(snap1, snap2, snap3)).flatten

  def receive = {
    case r: Rotation => 
      print(it.next()(r))
  }
}

final class Degree private (val self: Double) extends AnyVal {
  def +(other: Degree) = Degree(self + other.self)
  override def toString = {
    val str = self.toString
    str.substring(0, math.min(str.length, 6)) + "°"
  }
}

object Degree {
  def apply(v: Double): Degree =
    if (v < 0.0) new Degree(360.0 + (v % 360.0))
    else new Degree(v % 360.0)
  implicit def doubleToDegree(v: Double): Degree = apply(v)
}

case class Rotation(h: Degree, v: Degree) {
  def +(other: Rotation) = Rotation(h + other.h, v + other.v)
}

case object Start
case object Stop
case class RotateBy(r: Rotation)