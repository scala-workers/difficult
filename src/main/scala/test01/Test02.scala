package test01

import cats.*
import cats.effect.*
import com.github.kwhat.jnativehook.GlobalScreen
import com.github.kwhat.jnativehook.NativeHookException
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener
import fs2.*
import org.apache.pekko.actor.typed.ActorSystem
import sample.killrweather.fog.WeatherStation

import scala.concurrent.{Future, Promise}

trait CatchKeybordImpl {
  def catchFunc: Promise[Char]
  def toFuture: Future[Char]
  def toIO: IO[Char]
  def tail: CatchKeybordImpl
  var tailExtra: CatchKeybordImpl = null
}

object CatchKeybordImpl {
  def gen: CatchKeybordImpl = new CatchKeybordImpl {
    override val catchFunc: Promise[Char] = Promise[Char]
    override val toFuture: Future[Char]   = catchFunc.future
    override val toIO: IO[Char]           = IO.fromFuture(IO(toFuture))
    override def tail: CatchKeybordImpl   = tailExtra
  }
}

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
    val instance                           = CatchKeybordImpl.gen
    val actorSystem                        = ActorSystem.create(WeatherStation(instance), "uusdrlsdfsdnkwe")
    val listener: GlobalKeyListenerExample = new GlobalKeyListenerExample(actorSystem)

    val stream: Stream[IO, Char] = Stream.unfoldEval(instance) { ins =>
      for (r <- ins.toIO) yield Some(r -> ins.tail)
    }

    val action = stream.mapAsync(10)(t => IO(println(t)))

    IO.delay {
      try {
        GlobalScreen.registerNativeHook()
      } catch {
        case ex: NativeHookException =>
          System.err.println("There was a problem registering the native hook.")
          System.err.println(ex.getMessage())

          System.exit(1)
      }

      GlobalScreen.addNativeKeyListener(listener)
    }.flatTap(_ => action.compile.drain)
  }

}
