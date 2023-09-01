package test01

import cats.*
import cats.effect.*

import com.github.kwhat.jnativehook.GlobalScreen
import com.github.kwhat.jnativehook.NativeHookException
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener

class GlobalKeyListenerExample extends NativeKeyListener {
  override def nativeKeyPressed(e: NativeKeyEvent): Unit = {
    System.out.println("Key Pressed: " + NativeKeyEvent.getKeyText(e.getKeyCode()));

    if (e.getKeyCode() == NativeKeyEvent.VC_ESCAPE) {
      try {
        GlobalScreen.unregisterNativeHook();
      } catch {
        case nativeHookException: NativeHookException => nativeHookException.printStackTrace();
      }
    }
  }

  override def nativeKeyReleased(e: NativeKeyEvent): Unit = {
    System.out.println("Key Released: " + NativeKeyEvent.getKeyText(e.getKeyCode()))
  }

  override def nativeKeyTyped(e: NativeKeyEvent): Unit = {
    System.out.println("Key Typed: " + NativeKeyEvent.getKeyText(e.getKeyCode()))
  }
}

object Test02 extends IOApp.Simple {

  override val run: IO[Unit] = {
    IO.delay {
      try {
        GlobalScreen.registerNativeHook()
      } catch {
        case ex: NativeHookException =>
          System.err.println("There was a problem registering the native hook.")
          System.err.println(ex.getMessage())

          System.exit(1)
      }

      GlobalScreen.addNativeKeyListener(new GlobalKeyListenerExample())
    }.flatTap(_ => IO.never)
  }

}
