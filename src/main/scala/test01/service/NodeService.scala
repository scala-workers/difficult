package bb.cc

import cats.*
import cats.implicits.given
import cats.effect.*
import com.caoccao.javet.interop.NodeRuntime
import com.caoccao.javet.interop.engine.IJavetEngine
import com.caoccao.javet.node.modules.NodeModuleModule
import com.caoccao.javet.utils.JavetOSUtils
import com.github.kwhat.jnativehook.{GlobalScreen, NativeHookException}
import org.apache.pekko.Done
import org.apache.pekko.actor.typed.ActorSystem
import sample.killrweather.fog.WeatherStation
import test01.{GlobalKeyListenerExample, StreamDeal}
import fs2.*

import java.io.File
import scala.concurrent.Future

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
