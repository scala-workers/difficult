package bb.cc

import cats.*
import cats.implicits.given
import cats.effect.*
import com.caoccao.javet.annotations.{V8Function, V8Property}
import com.caoccao.javet.interop.NodeRuntime
import com.caoccao.javet.utils.JavetOSUtils
import com.caoccao.javet.interop.engine.{IJavetEngine, IJavetEnginePool, JavetEnginePool}
import com.caoccao.javet.enums.JSRuntimeType
import com.caoccao.javet.node.modules.NodeModuleModule
import com.caoccao.javet.values.reference.{V8ValueGlobalObject, V8ValueObject}
import org.apache.pekko.Done
import test01.node_runtime.{JavetEngineWrap, V21AAA}
import test01.service.SetVolumeService

import java.io.File
import scala.concurrent.Promise

object Test01 extends IOApp.Simple:

  def runDone[F[_]: Async]: F[Done] =
    val runtimeResource = for
      given IJavetEnginePool[NodeRuntime] <- V21AAA.resource[F]
      given IJavetEngine[NodeRuntime]     <- JavetEngineWrap(summon).resource[F]
      given Resource[F, NodeRuntime] = ToNodeRuntime(summon).resource[F]
    yield SetVolumeService(summon)

    runtimeResource.use(v => v.setVolume)
  end runDone

  def toUNIt[F[_]: Functor](f: F[Done]): F[Unit] = for (done: Done <- f) yield done

  override val run: IO[Unit] = toUNIt(runDone[IO])

end Test01
