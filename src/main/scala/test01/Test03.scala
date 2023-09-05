package sample.killrweather.fog

import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.scaladsl.ActorContext
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.scaladsl.LoggerOps
import org.apache.pekko.http.scaladsl.Http
import org.apache.pekko.http.scaladsl.client.RequestBuilding.Post
import org.apache.pekko.http.scaladsl.unmarshalling.Unmarshal
import org.apache.pekko.stream.SystemMaterializer

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.Failure
import scala.util.Random
import scala.util.Success

/** How many weather stations there are? Currently: "well over 10,000 manned and automatic surface weather stations, 1,000 upper-air
  * stations, 7,000 ships, 100 moored and 1,000 drifting buoys, hundreds of weather radars and 3,000 specially equipped commercial aircraft
  * measure key parameters of the atmosphere, land and ocean surface every day. Add to these some 16 meteorological and 50 research
  * satellites to get an idea of the size of the global network for meteorological, hydrological and other geophysical observations."
  *   - https://public.wmo.int/en/our-mandate/what-we-do/observations
  */
/*private[fog] object WeatherStation {

  type WeatherStationId = String

  sealed trait Command
  case object Sample                                    extends Command
  private final case class ProcessSuccess(msg: String)  extends Command
  private final case class ProcessFailure(e: Throwable) extends Command

  /** Starts a device and it's task to initiate reading data at a scheduled rate. */
  def apply(wsid: WeatherStationId, settings: FogSettings, httpPort: Int): Behavior[Command] =
    Behaviors.setup(ctx => new WeatherStation(ctx, wsid, settings, httpPort).running(httpPort))
}

/** Starts a device and it's task to initiate reading data at a scheduled rate. */
private class WeatherStation(
  context: ActorContext[WeatherStation.Command],
  wsid: WeatherStation.WeatherStationId,
  settings: FogSettings,
  httpPort: Int
) {
  import WeatherStation._

  private val random = new Random()

  private val http = {
    import org.apache.pekko.actor.typed.scaladsl.adapter._
    Http(context.system.toClassic)
  }
  private val stationUrl = s"http://${settings.host}:${httpPort}/weather/$wsid"

  def running(httpPort: Int): Behavior[WeatherStation.Command] = {
    context.log.infoN(s"Started WeatherStation {} of total {} with weather port {}", wsid, settings.weatherStations, httpPort)

    Behaviors.setup[WeatherStation.Command] { context =>
      context.log.debugN(s"Started {} data sampling.", wsid)

      Behaviors.withTimers { timers =>
        timers.startSingleTimer(Sample, Sample, settings.sampleInterval)

        Behaviors.receiveMessage {
          case Sample =>
            val value     = 5 + 30 * random.nextDouble
            val eventTime = System.currentTimeMillis
            context.log.debug("Recording temperature measurement {}", value)
            recordTemperature(eventTime, value)
            Behaviors.same

          case ProcessSuccess(msg) =>
            context.log.debugN("Successfully registered data: {}", msg)
            // trigger next sample only after we got a successful response
            timers.startSingleTimer(Sample, Sample, settings.sampleInterval)
            Behaviors.same

          case ProcessFailure(e) =>
            throw new RuntimeException("Failed to register data", e)
        }
      }
    }
  }

  private def recordTemperature(eventTime: Long, temperature: Double): Unit = {
    implicit val ec           = context.executionContext
    implicit val materializer = SystemMaterializer(context.system).materializer

    // we could also use a class and a Json formatter like in the server
    // but since this is the only json we send this is a bit more concise
    import spray.json._
    import org.apache.pekko.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
    val json = JsObject("eventTime" -> JsNumber(eventTime), "dataType" -> JsString("temperature"), "value" -> JsNumber(temperature))

    val futureResponseBody: Future[String] = http
      .singleRequest(Post(stationUrl, json))
      .flatMap(res =>
        Unmarshal(res)
          .to[String]
          .map(body =>
            if (res.status.isSuccess()) body
            else throw new RuntimeException(s"Failed to register data: $body")
          )
      )
    context.pipeToSelf(futureResponseBody) {
      case Success(s) => ProcessSuccess(s)
      case Failure(e) => ProcessFailure(e)
    }

  }

}*/

import cats.effect._

trait CatchEvent[T] {
  def catchFunc: Promise[T]
  def tail: CatchEvent[T]
}

trait WrapIOEvent[T] {
  def toFuture: Future[T]
  def tail: WrapIOEvent[T]
}

trait IOEventHandle[T] {
  def toIO: IO[T]
  def tail: IOEventHandle[T]
}

trait CatchEventImpl[T] {
  def catchFunc: Either[Exception, T] => Unit
}

trait WrapIOEventImpl[T] {
  def toIO: IO[T]
}

object bb extends IOApp.Simple {

  def sjbjerjn[T](t: (Either[Exception, T] => Unit) => Unit): IO[T] = IO.async {
    t.andThen(_ => IO(Option.empty))
  }

  def xbxb[T](implicit ctx: ExecutionContext): (Future[CatchEventImpl[T]], WrapIOEventImpl[T]) = {
    val p: Promise[CatchEventImpl[T]] = Promise[CatchEventImpl[T]]
    val ioT: IO[T] = IO.async(cb =>
      IO {
        p.trySuccess(new CatchEventImpl[T] {
          override val catchFunc: Either[Exception, T] => Unit = cb
        })
        Option.empty
      }
    )
    (
      p.future,
      new WrapIOEventImpl[T] {
        override def toIO: IO[T] = ioT
      }
    )
  }

  def xx[T]: (CatchEventImpl[T] => IO[Unit]) => WrapIOEventImpl[T] = { (callback: CatchEventImpl[T] => IO[Unit]) =>
    val ioT: IO[T] = IO.async((cc: Either[Exception, T] => Unit) =>
      for (
        t <- callback
          .compose((u: Either[Exception, T] => Unit) =>
            new CatchEventImpl[T] {
              override def catchFunc: Either[Exception, T] => Unit = u
            }
          )
          .apply(cc)
      ) yield Option.empty
    )

    new WrapIOEventImpl[T] {
      override def toIO: IO[T] = ioT
    }
  }

  val cc: IO[String] = IO.async { cb =>
    IO {
      cb(Right("55" * 100))
      Option.empty
    }
  }

  override val run: IO[Unit] = {
    val xu  = sjbjerjn[String]
    val aab = xu(catchE => catchE(Right("xxxx" * 50)))
    for (str <- aab) yield println(str)
  }

}
