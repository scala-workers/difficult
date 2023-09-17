package bb.cc

import cats.*
import cats.implicits.given
import cats.effect.*
import com.caoccao.javet.interop.NodeRuntime
import com.caoccao.javet.interop.engine.{IJavetEngine, IJavetEnginePool}
import org.apache.pekko.Done
import test01.node_runtime.JavetEngineWrap
import test01.service.SetVolumeService

import java.io.File
import scala.concurrent.Promise

object Test01 extends IOApp.Simple:

  def runDone[F[_]: Async]: F[Done] =
    val runtimeResource = for
      given ToNodeRuntime <- V21AAA.resource[F]
      given Resource[F, NodeRuntime] = summon[ToNodeRuntime].resource[F]
    yield SetVolumeService(summon)

    runtimeResource.use(v => v.setVolume)
  end runDone

  def toUNIt[F[_]: Functor](f: F[Done]): F[Unit] = for (done: Done <- f) yield done

  override val run: IO[Unit] = toUNIt(runDone[IO])

end Test01
