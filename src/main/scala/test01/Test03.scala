package sample.killrweather.fog

import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors, LoggerOps}
import org.apache.pekko.http.scaladsl.Http
import org.apache.pekko.http.scaladsl.client.RequestBuilding.Post
import org.apache.pekko.http.scaladsl.unmarshalling.Unmarshal
import org.apache.pekko.stream.SystemMaterializer
import test01.CatchKeybordImpl

object WeatherStation:
  sealed trait Command
  final case class CommandKey(keyCode: Char) extends Command

  def apply(catcher: CatchKeybordImpl): Behavior[Command] =
    Behaviors.setup(ctx => new WeatherStation(ctx, catcher = catcher))
end WeatherStation

class WeatherStation(context: ActorContext[WeatherStation.Command], catcher: CatchKeybordImpl)
    extends AbstractBehavior[WeatherStation.Command](context):

  override def onMessage(msg: WeatherStation.Command): Behavior[WeatherStation.Command] =
    msg.match
      case WeatherStation.CommandKey(key) =>
        catcher.catchFunc.trySuccess(key)
        WeatherStation(catcher.tail)

end WeatherStation
