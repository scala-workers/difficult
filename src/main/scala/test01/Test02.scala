package test01

import bb.cc.{ActorSystemResources, CatchKeybordImpl}
import cats.*
import cats.syntax.*
import cats.implicits.given
import cats.effect.*
import com.github.kwhat.jnativehook.GlobalScreen
import com.github.kwhat.jnativehook.NativeHookException
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener
import fs2.*
import org.apache.pekko.Done
import org.apache.pekko.actor.typed.ActorSystem
import sample.killrweather.fog.WeatherStation

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

  class ExecImpl(actorSys: ActorSystem[WeatherStation.Command], instance: Future[CatchKeybordImpl]):

    val listener: GlobalKeyListenerExample = new GlobalKeyListenerExample(actorSys)

    val streamPrepare: Stream[IO, Char] = Stream.unfoldEval(IO.fromFuture(IO(instance)))(_.match
      case ins =>
        for
          model        <- ins
          charInstance <- model.toIO
        yield Some(charInstance -> IO.fromFuture(IO(model.tail)))
    )

    val action = StreamDeal(streamPrepare).mapAsync

    val execAction: IO[Unit] = IO
      .delay {
        try {
          GlobalScreen.registerNativeHook()
        } catch {
          case ex: NativeHookException =>
            System.err.println("There was a problem registering the native hook.")
            System.err.println(ex.getMessage())

            System.exit(1)
        }

        GlobalScreen.addNativeKeyListener(listener)
      }
      .flatTap(_ => action.compile.drain)

  end ExecImpl

  override val run: IO[Unit] = {
    import scala.concurrent.ExecutionContext.Implicits.given
    val instance = Future.successful(CatchKeybordImpl.gen)

    def actorSystemInstanceGen: ActorSystem[WeatherStation.Command]     = ActorSystem.create(WeatherStation(instance), "uusdrlsdfsdnkwe")
    val sysResources: Resource[IO, ActorSystem[WeatherStation.Command]] = ActorSystemResources(IO(actorSystemInstanceGen)).resource

    sysResources.use(actorSys => ExecImpl(actorSys, instance).execAction)
  }

}
