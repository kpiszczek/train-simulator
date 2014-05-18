package kubap.trains

import akka.actor.{ ActorRef, ActorSystem, Props, Actor, Cancellable}
import concurrent.duration._
import akka.kernel.Bootable
import language.implicitConversions

class Train(world: ActorRef, horizontalSpeed: Degree, verticalSpeed: Degree) extends Actor {
  import scala.concurrent.ExecutionContext.Implicits.global

  var cancellable: Option[Cancellable] = None
  
  val stopped: Receive = {
    case Start => 
      cancellable = Some(context.system.scheduler.schedule(
        0.second, 0.5.seconds, world, RotateBy(Rotation(horizontalSpeed, verticalSpeed))))
      context.become(running)
  }  

  val running: Receive = {
    case Stop => {
      cancellable match {
        case Some(c) => c.cancel()
        case None => 
      }
      context.become(stopped)
    }
  }

  def receive = stopped
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

class HelloTrains extends Bootable {
  val universe = ActorSystem("universe")

  def startup = {
    val world = universe.actorOf(Props[World], name = "world")
    val train = universe.actorOf(Props(new Train(world, 0.01, 0.005)), name = "train")
    
    train ! Start

    sys.addShutdownHook(universe.shutdown())
  }

  def shutdown = {
    universe.shutdown()
  }
}

object Main extends App {
  (new HelloTrains).startup()
}

class Printer extends Actor {
  val snap1 = (r: Rotation) => s"""
        ooOOO            Pociąg obraca Dysk pod sobą,
       oo      _____     samemu pozostając w bezruchu.
      _I__n_n__||_||     Aktualny obrót Dysku to: ${r.h}N ${r.v}W
    >(_________|_7_|     
     /o ()() ()() o 
≠≠≠≠≠≠≠≠≠≠≠≠≠≠≠≠≠≠≠≠≠≠≠
·     ·     ·     ·        
  """

  val snap2 = (r: Rotation) => s"""
        ooooo            Pociąg obraca Dysk pod sobą,
       OO      _____     samemu pozostając w bezruchu.
      _I__n_n__||_||     Aktualny obrót Dysku to: ${r.h}N ${r.v}W
    >(_________|_7_|
     /O ()() ()() o 
≠≠≠≠≠≠≠≠≠≠≠≠≠≠≠≠≠≠≠≠≠≠≠
  ·     ·     ·     ·   
  """

  val snap3 = (r: Rotation) => s"""
        OOOoo            Pociąg obraca Dysk pod sobą,
       oo      _____     samemu pozostając w bezruchu.
      _I__n_n__||_||     Aktualny obrót Dysku to: ${r.h}N ${r.v}W
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