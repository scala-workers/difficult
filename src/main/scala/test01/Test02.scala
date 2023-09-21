package test01

import bb.cc.{ActorSystemResources, CatchKeybordImpl, ExecImpl, ToNodeRuntime, V21AAA}
import cats.*
import cats.syntax.*
import cats.implicits.given
import cats.effect.*
import com.caoccao.javet.interop.NodeRuntime
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener
import fs2.*
import org.apache.pekko.Done
import org.apache.pekko.actor.typed.ActorSystem
import sample.killrweather.fog.WeatherStation
import test01.node_runtime.LoudnessService
import test01.service.{SetVolCusFunction, SetVolumeService, ToFunction}

import scala.concurrent.{ExecutionContext, Future, Promise}

class GlobalKeyListenerExample(val instance: ActorSystem[WeatherStation.Command]) extends NativeKeyListener {

  override def nativeKeyPressed(e: NativeKeyEvent): Unit = {
    // System.out.println("Key Pressed: " + NativeKeyEvent.getKeyText(e.getKeyCode()))
  }

  override def nativeKeyReleased(e: NativeKeyEvent): Unit = {
    // System.out.println("Key Released: " + NativeKeyEvent.getKeyText(e.getKeyCode()))
  }

  override def nativeKeyTyped(e: NativeKeyEvent): Unit = {
    instance ! WeatherStation.CommandKey(e.getKeyChar())
    // System.out.println("Key Typed: " + e.getKeyChar())
  }

}

object Test0211111111111 extends IOApp.Simple:

  override val run: IO[Unit] =
    import scala.concurrent.ExecutionContext.Implicits.given
    val instance = Future.successful(CatchKeybordImpl.gen)

    val actorSystemInstanceGen: IO[ActorSystem[WeatherStation.Command]] = IO(
      ActorSystem.create(WeatherStation(instance), "uusdrlsdfsdnkwe")
    )
    val sysResources: Resource[IO, ActorSystem[WeatherStation.Command]] = ActorSystemResources(actorSystemInstanceGen).resource

    val runtimeResource: Resource[IO, LoudnessService[IO]] = for
      given ToNodeRuntime <- V21AAA.resource1[IO]
      given NodeRuntime   <- summon[ToNodeRuntime].resource[IO]
      setService          <- test01.node_runtime.LoudnessServiceImpl4F(summon).resource[IO]
    yield setService

    val execImpl = for
      given ActorSystem[WeatherStation.Command] <- sysResources
      given LoudnessService[IO]                 <- runtimeResource
    yield ExecImpl(summon, instance, summon)

    execImpl.use(_.execAction)
  end run

end Test0211111111111
