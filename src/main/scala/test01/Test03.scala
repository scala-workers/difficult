package sample.killrweather.fog

import org.apache.pekko.actor.typed.Behavior
import org.apache.pekko.actor.typed.scaladsl.ActorContext
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.scaladsl.LoggerOps
import org.apache.pekko.http.scaladsl.Http
import org.apache.pekko.http.scaladsl.client.RequestBuilding.Post
import org.apache.pekko.http.scaladsl.unmarshalling.Unmarshal
import org.apache.pekko.stream.SystemMaterializer
import test01.CatchKeybordImpl

object WeatherStation {
  sealed trait Command
  final case class CommandKey(keyCode: Char) extends Command

  def apply(catcher: CatchKeybordImpl): Behavior[Command] =
    Behaviors.setup(ctx => new WeatherStation(ctx, catcher = catcher).running)
}

class WeatherStation(context: ActorContext[WeatherStation.Command], catcher: CatchKeybordImpl) {

  def running: Behavior[WeatherStation.Command] = Behaviors.receiveMessage { case WeatherStation.CommandKey(key) =>
    catcher.catchFunc.trySuccess(key)
    WeatherStation(catcher.tail)
  }

}
