package test01

import sample.killrweather.fog.*
import cats.*
import cats.effect.*
import com.github.kwhat.jnativehook.GlobalScreen
import com.github.kwhat.jnativehook.NativeHookException
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener
import fs2._

import scala.concurrent.{Future, Promise}

class GlobalKeyListenerExample extends NativeKeyListener {
  trait SetterImpl {
    def tail: WrapIOEvent[Char] with CatchEvent[Char] with IOEventHandle[Char] with SetterImpl      = tailExtra
    var tailExtra: WrapIOEvent[Char] with CatchEvent[Char] with IOEventHandle[Char] with SetterImpl = null
  }

  def gen: WrapIOEvent[Char] with CatchEvent[Char] with IOEventHandle[Char] with SetterImpl = new WrapIOEvent[Char]
    with CatchEvent[Char]
    with IOEventHandle[Char]
    with SetterImpl {
    override val catchFunc: Promise[Char]                                                               = Promise[Char]
    override val toFuture: Future[Char]                                                                 = catchFunc.future
    override val toIO: IO[Char]                                                                         = IO.fromFuture(IO(toFuture))
    override def tail: WrapIOEvent[Char] with CatchEvent[Char] with IOEventHandle[Char] with SetterImpl = super.tail
  }

  var instance: WrapIOEvent[Char] with CatchEvent[Char] with IOEventHandle[Char] with SetterImpl = gen

  override def nativeKeyPressed(e: NativeKeyEvent): Unit = {
    /*System.out.println("Key Pressed: " + NativeKeyEvent.getKeyText(e.getKeyCode()))

    if (e.getKeyCode() == NativeKeyEvent.VC_ESCAPE) {
      try {
        GlobalScreen.unregisterNativeHook();
      } catch {
        case nativeHookException: NativeHookException => nativeHookException.printStackTrace()
      }
    }*/
  }

  override def nativeKeyReleased(e: NativeKeyEvent): Unit = {
    // System.out.println("Key Released: " + NativeKeyEvent.getKeyText(e.getKeyCode()))
  }

  override def nativeKeyTyped(e: NativeKeyEvent): Unit = {
    this.synchronized {
      val catchInstance = instance
      instance.tailExtra = gen
      instance = instance.tailExtra
      catchInstance.catchFunc.trySuccess(e.getKeyChar())
    }
    // System.out.println("Key Typed: " + e.getKeyChar())
  }
}

object Test0211111111111 extends IOApp.Simple {

  override val run: IO[Unit] = {
    val listener: GlobalKeyListenerExample = new GlobalKeyListenerExample()
    val stream: Stream[IO, Char] = Stream.unfoldEval(listener.instance) { instance =>
      for (r <- instance.toIO) yield Some(r -> instance.tail)
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
