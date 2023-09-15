package test01

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

trait CatchKeybordImpl {
  def inputKeyAndNext(charInstance: Char): Unit = catchFunc.trySuccess(charInstance)

  protected val catchFunc: Promise[Char] = Promise[Char]
  protected val toFuture: Future[Char]   = catchFunc.future
  def toIO: IO[Char]                     = IO.fromFuture(IO(toFuture))
  val tail: Future[CatchKeybordImpl]
}

object CatchKeybordImpl {
  def gen(using ExecutionContext): CatchKeybordImpl = new CatchKeybordImpl {
    self =>
    override val tail: Future[CatchKeybordImpl] = for _: Char <- self.toFuture yield gen
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
    val instance = Future.successful(CatchKeybordImpl.gen(Future.successful(' ')))

    def actorSystemInstanceGen: ActorSystem[WeatherStation.Command]     = ActorSystem.create(WeatherStation(instance), "uusdrlsdfsdnkwe")
    val sysResources: Resource[IO, ActorSystem[WeatherStation.Command]] = ActorSystemResources(IO(actorSystemInstanceGen)).resource

    sysResources.use(actorSys => ExecImpl(actorSys, instance).execAction)
  }

}

class ActorSystemResources[F[_], -T](actorSystem: F[ActorSystem[T]]) {

  private def closeAction[UF[_]: Async, U](actorSys: ActorSystem[U]): UF[Done] = for
    unitDone              <- Sync[UF].delay(actorSys.terminate())
    closeActionDone: Done <- Async[UF].fromFuture(Sync[UF].delay(actorSys.whenTerminated))
  yield closeActionDone: Done

  def resource(using Async[F]): Resource[F, ActorSystem[T]] =
    Resource.make(actorSystem)(sys => for doneToUnit <- closeAction(sys) yield doneToUnit: Done)

}
