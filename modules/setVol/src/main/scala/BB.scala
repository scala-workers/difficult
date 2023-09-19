package bb.cc

import cats._
import cats.effect._
import cats.implicits._
import com.caoccao.javet.enums.JSRuntimeType
import com.caoccao.javet.interop.NodeRuntime
import com.caoccao.javet.interop.engine.{IJavetEnginePool, JavetEnginePool}
import com.caoccao.javet.node.modules.NodeModuleModule
import com.caoccao.javet.utils.JavetOSUtils

import net.scalax.ScalaxDone

import java.io.File

class ToNodeRuntime(pool: IJavetEnginePool[NodeRuntime]) {

  private def resourceImpl[F[_]: Sync]: Resource[F, NodeRuntime] = Resource.fromAutoCloseable(Sync[F].delay(pool.getEngine.getV8Runtime))

  private def inputModule[F[_]: Sync](rumTime: NodeRuntime): Resource[F, NodeModuleModule] =
    Resource.fromAutoCloseable(Sync[F].delay(rumTime.getNodeModule(classOf[NodeModuleModule])))

  private def setModuleRootResource[F[_]: Sync](nodeModuleModule: NodeModuleModule): Resource[F, ScalaxDone] = {
    val setterAction: F[ScalaxDone] = Sync[F].delay {
      val workingDirectory: File = new File(new File(JavetOSUtils.WORKING_DIRECTORY, "nodeTemp"), "node_modules")
      nodeModuleModule.setRequireRootDirectory(workingDirectory)
      ScalaxDone
    }
    val liftK = Resource.liftK[F]
    liftK(setterAction)
  }

  def resource[F[_]: Sync]: Resource[F, NodeRuntime] = for {
    runtimeInstance                    <- resourceImpl
    nodeModuleModule: NodeModuleModule <- inputModule(runtimeInstance)
    scalaxDone: ScalaxDone             <- setModuleRootResource(nodeModuleModule)
  } yield runtimeInstance

}

object V21AAA {

  def resource[F[_]: Sync]: Resource[F, ToNodeRuntime] = {
    val instance = Sync[F].delay {
      val pool = new JavetEnginePool[NodeRuntime]
      pool.getConfig.setJSRuntimeType(JSRuntimeType.Node)
      pool
    }
    val poolResource = Resource.fromAutoCloseable(instance)

    for (pool <- poolResource) yield new ToNodeRuntime(pool)
  }

}
