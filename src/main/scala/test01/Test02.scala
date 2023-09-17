package test01

import bb.cc.{ActorSystemResources, CatchKeybordImpl, ExecImpl, ToNodeRuntime, V21AAA}
import cats.*
import cats.syntax.*
import cats.implicits.given
import cats.effect.*
import com.caoccao.javet.interop.NodeRuntime
import com.github.kwhat.jnativehook.GlobalScreen
import com.github.kwhat.jnativehook.NativeHookException
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener
import fs2.*
import org.apache.pekko.Done
import org.apache.pekko.actor.typed.ActorSystem
import sample.killrweather.fog.WeatherStation
import com.caoccao.javet.interop.engine.{IJavetEngine, IJavetEnginePool, JavetEnginePool}
import test01.node_runtime.JavetEngineWrap
import test01.service.SetVolumeService

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

object Test0211111111111 extends IOApp.Simple {

  override val run: IO[Unit] = {
    import scala.concurrent.ExecutionContext.Implicits.given
    val instance = Future.successful(CatchKeybordImpl.gen)

    def actorSystemInstanceGen: ActorSystem[WeatherStation.Command]     = ActorSystem.create(WeatherStation(instance), "uusdrlsdfsdnkwe")
    val sysResources: Resource[IO, ActorSystem[WeatherStation.Command]] = ActorSystemResources(IO(actorSystemInstanceGen)).resource

    val runtimeResource: Resource[IO, Resource[IO, NodeRuntime]] = for
      given IJavetEnginePool[NodeRuntime] <- V21AAA.resource[IO]
      given IJavetEngine[NodeRuntime]     <- JavetEngineWrap(summon).resource[IO]
      given Resource[IO, NodeRuntime] = ToNodeRuntime(summon).resource[IO]
    yield summon

    val execImpl = for
      given ActorSystem[WeatherStation.Command] <- sysResources
      nodeRuntime                               <- runtimeResource
      given SetVolumeService[IO] = SetVolumeService(nodeRuntime)
    yield ExecImpl(summon, instance, summon)

    execImpl.use(_.execAction)
  }

}
