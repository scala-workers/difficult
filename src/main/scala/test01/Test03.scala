package sample.killrweather.fog

import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors, LoggerOps}
import org.apache.pekko.http.scaladsl.Http
import org.apache.pekko.http.scaladsl.client.RequestBuilding.Post
import org.apache.pekko.http.scaladsl.unmarshalling.Unmarshal
import org.apache.pekko.stream.SystemMaterializer
import test01.CatchKeybordImpl

import scala.concurrent.Future

object WeatherStation:
  sealed trait Command
  final case class CommandKey(keyCode: Char) extends Command

  def apply(catcher: Future[CatchKeybordImpl]): Behavior[Command] =
    Behaviors.setup(ctx => new WeatherStation(ctx, catcher = catcher))
end WeatherStation

class WeatherStation(context: ActorContext[WeatherStation.Command], catcher: Future[CatchKeybordImpl])
    extends AbstractBehavior[WeatherStation.Command](context):

  import scala.concurrent.ExecutionContext.Implicits.given

  override def onMessage(msg: WeatherStation.Command): Behavior[WeatherStation.Command] =
    msg.match
      case WeatherStation.CommandKey(key) =>
        for current <- catcher yield current.inputKeyAndNext(key)

        val nextFuture = for current <- catcher; nextModel <- current.tail yield nextModel

        WeatherStation(nextFuture)

end WeatherStation
