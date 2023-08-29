package bb
package cc

import java.awt.Robot
import cats.*
import cats.effect.*
import com.caoccao.javet.interop.{NodeRuntime, V8Host}
import com.caoccao.javet.utils.JavetOSUtils
import com.caoccao.javet.interop.engine.JavetEnginePool
import com.caoccao.javet.enums.JSRuntimeType
import com.caoccao.javet.node.modules.NodeModuleModule

import java.io.File

object Test01 extends IOApp.Simple {

  class V8RuntimeBuilder(host: V8Host) {
    val instance: Resource[IO, NodeRuntime] = Resource.fromAutoCloseable(IO.delay(host.createV8Runtime: NodeRuntime))
  }

  val v8Instance: Resource[IO, V8Host] = Resource.fromAutoCloseable(IO.delay(V8Host.getNodeInstance()))

  def exec(nodeRuntime: NodeRuntime): IO[Unit] = {
    val execString: String =
      """
        |const ioHook = require('iohook'); ioHook.on("keypress", event => { console.log(event); }); ioHook.start();
        |""".stripMargin
    def execAction: Unit = {
      println(JavetOSUtils.WORKING_DIRECTORY)
      nodeRuntime.getExecutor(execString).executeVoid()
      nodeRuntime.await()
    }
    IO.delay(System.out.println("1 + 1 = " + nodeRuntime.getExecutor("1 + 1").executeInteger()))
    IO.delay(execAction)
  }

  override val run: IO[Unit] = {
    val runtimeResource = for {
      given V8Host <- v8Instance
      nodeRuntime  <- V8RuntimeBuilder(summon).instance
    } yield {
      val workingDirectory: File = new File(JavetOSUtils.WORKING_DIRECTORY, "nodeTemp")
      // Set the require root directory so that Node.js is able to locate node_modules.
      nodeRuntime.getNodeModule(classOf[NodeModuleModule]).setRequireRootDirectory(workingDirectory)
      nodeRuntime
    }

    runtimeResource.use(v => exec(v))
  }

}
