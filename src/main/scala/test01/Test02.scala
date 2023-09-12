package test01

import cats.*
import cats.syntax.*
import cats.implicits._
import cats.effect.*
import com.github.kwhat.jnativehook.GlobalScreen
import com.github.kwhat.jnativehook.NativeHookException
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener
import fs2.*
import org.apache.pekko.Done
import org.apache.pekko.actor.typed.ActorSystem
import sample.killrweather.fog.WeatherStation

import scala.concurrent.{Future, Promise}

trait CatchKeybordImpl {
  def catchFunc: Promise[Char]
  def toFuture: Future[Char]
  def toIO: IO[Char]
  def tail: CatchKeybordImpl
}

object CatchKeybordImpl {
  def gen: CatchKeybordImpl = new CatchKeybordImpl {
    override val catchFunc: Promise[Char]    = Promise[Char]
    override val toFuture: Future[Char]      = catchFunc.future
    override val toIO: IO[Char]              = IO.fromFuture(IO(toFuture))
    override lazy val tail: CatchKeybordImpl = gen
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

  class ExecImpl(actorSys: ActorSystem[WeatherStation.Command], instance: CatchKeybordImpl) {
    val listener: GlobalKeyListenerExample = new GlobalKeyListenerExample(actorSys)

    val stream: Stream[IO, Char] = Stream.unfoldEval(instance) { ins =>
      for (r <- ins.toIO) yield Some(r -> ins.tail)
    }

    val action = stream.mapAsync(10)(t => IO(println(t)))

    val execAction = IO
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
  }

  override val run: IO[Unit] = {
    val instance = CatchKeybordImpl.gen

    def actorSystemInstanceGen = ActorSystem.create(WeatherStation(instance), "uusdrlsdfsdnkwe")
    val actorSystemResource: Resource[IO, ActorSystem[WeatherStation.Command]] = ActorSystemResources(IO(actorSystemInstanceGen)).resource

    actorSystemResource.use(actorSys => ExecImpl(actorSys, instance).execAction)
  }

}

class ActorSystemResources[F[_], T](val actorSystem: F[ActorSystem[T]]) {
  private def closeAction[UF[_]: Async, U](actorSys: ActorSystem[U]): UF[Done] = for
    unitDone   <- Sync[UF].delay(actorSys.terminate())
    done: Done <- Async[UF].fromFuture(Sync[UF].delay(actorSys.whenTerminated))
  yield done

  private def toUNIT[UF[_]: Functor, U](ueffect: UF[U]): UF[Unit] = for (tModel: U <- ueffect) yield tModel

  def resource(using Async[F]): Resource[F, ActorSystem[T]] = Resource.make(actorSystem)(sys => toUNIT(closeAction(sys)))
}
