package bb.cc

import cats.*
import cats.implicits.given
import cats.effect.*
import com.caoccao.javet.enums.JSRuntimeType
import com.caoccao.javet.interop.NodeRuntime
import com.caoccao.javet.interop.engine.{IJavetEngine, IJavetEnginePool, JavetEnginePool}
import com.caoccao.javet.node.modules.NodeModuleModule
import com.caoccao.javet.utils.JavetOSUtils
import org.apache.pekko.Done

import java.io.File

class ToNodeRuntime(pool: IJavetEnginePool[NodeRuntime]):

  private def resourceImpl[F[_]: Sync]: Resource[F, NodeRuntime] = Resource.fromAutoCloseable(Sync[F].delay(pool.getEngine.getV8Runtime))

  private def inputModule[F[_]: Sync](rumTime: NodeRuntime): Resource[F, NodeModuleModule] =
    Resource.fromAutoCloseable(Sync[F].delay(rumTime.getNodeModule(classOf[NodeModuleModule])))

  private def setModuleRootResource[F[_]: Sync](nodeModuleModule: NodeModuleModule): Resource[F, Done] =
    val setterAction = Sync[F].delay(().match
      case _ =>
        val workingDirectory: File = new File(new File(JavetOSUtils.WORKING_DIRECTORY, "nodeTemp"), "node_modules")
        nodeModuleModule.setRequireRootDirectory(workingDirectory)
        Done
    )
    Resource.eval(setterAction)
  end setModuleRootResource

  def resource[F[_]: Sync]: Resource[F, NodeRuntime] = for
    runtimeInstance  <- resourceImpl
    nodeModuleModule <- inputModule(runtimeInstance)
    done: Done       <- setModuleRootResource(nodeModuleModule)
  yield runtimeInstance

end ToNodeRuntime

object V21AAA:
  def resource[F[_]: Sync]: Resource[F, ToNodeRuntime] =
    val poolResource = Resource.fromAutoCloseable(Sync[F].delay(().match
      case _ =>
        val pool = JavetEnginePool[NodeRuntime]()
        pool.getConfig().setJSRuntimeType(JSRuntimeType.Node)
        pool
    ))
    for pool <- poolResource yield ToNodeRuntime(pool)
end V21AAA
