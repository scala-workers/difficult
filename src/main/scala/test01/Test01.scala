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

import java.io.File
import scala.concurrent.Promise

class HaveATest(catchFunc: Done => Unit, volumeValue: Int) {
  @V8Property
  def getVolumeValue: Int = volumeValue

  @V8Function(name = "finished")
  def finished: Unit = {
    println("喵呜呜完成啦lalalalala")
    catchFunc(Done)
  }
}

object Test01 extends IOApp.Simple:

  def runDone[F[_]: Async]: F[Done] =
    val runtimeResource = for
      given IJavetEnginePool[NodeRuntime] <- V21AAA.resource[F]
      given IJavetEngine[NodeRuntime]     <- V1aa(summon).resource[F]
      given Resource[F, NodeRuntime] = ToNodeRuntime(summon).resource[F]
    yield ExecAction(summon)

    runtimeResource.use(v => v.setVolume)
  end runDone

  def toUNIt[F[_]: Functor](f: F[Done]): F[Unit] = for (done: Done <- f) yield done

  override val run: IO[Unit] = toUNIt(runDone[IO])

end Test01

class V1aa(bb: IJavetEnginePool[NodeRuntime]):
  def resource[F[_]: Sync]: Resource[F, IJavetEngine[NodeRuntime]] = Resource.fromAutoCloseable(Sync[F].delay(bb.getEngine))
end V1aa

object V21AAA:
  def resource[F[_]: Sync]: Resource[F, IJavetEnginePool[NodeRuntime]] =
    Resource.fromAutoCloseable(Sync[F].delay(().match
      case _ =>
        val pool = JavetEnginePool[NodeRuntime]()
        pool.getConfig().setJSRuntimeType(JSRuntimeType.Node)
        pool
    ))
end V21AAA

class ToNodeRuntime(pool: IJavetEngine[NodeRuntime]):

  private def resourceImpl[F[_]: Sync]: Resource[F, NodeRuntime] = Resource.fromAutoCloseable(Sync[F].delay(pool.getV8Runtime))

  private def inputModule[F[_]: Sync](rumTime: NodeRuntime): Resource[F, NodeModuleModule] =
    Resource.fromAutoCloseable(Sync[F].delay(rumTime.getNodeModule(classOf[NodeModuleModule])))

  private def setModuleRoot[F[_]: Sync](nodeModuleModule: NodeModuleModule): F[Done] =
    Sync[F].delay(().match
      case _ =>
        val workingDirectory: File = new File(new File(JavetOSUtils.WORKING_DIRECTORY, "nodeTemp"), "node_modules")
        nodeModuleModule.setRequireRootDirectory(workingDirectory)
        Done
    )
  end setModuleRoot

  private def setModuleRootResource[F[_]: Sync](nodeModuleModule: NodeModuleModule): Resource[F, Done] =
    Resource.eval(setModuleRoot(nodeModuleModule))

  def resource[F[_]: Sync]: Resource[F, NodeRuntime] = for
    runtimeImpl      <- resourceImpl
    nodeModuleModule <- inputModule(runtimeImpl)
    done: Done       <- setModuleRootResource(nodeModuleModule)
  yield runtimeImpl

end ToNodeRuntime

class ExecAction[F[_]](nodeRuntimeResource: Resource[F, NodeRuntime]):
  def setVolume(using Async[F]): F[Done] = nodeRuntimeResource.use(runtime => ExecActionImpl(runtime).action)
end ExecAction

class ExecActionImpl(nodeRuntime: NodeRuntime):

  def action[F[_]: Async]: F[Done] =
    val execString: String =
      """
        |const loudness = require('loudness')
        |async function setV(numValue) {
        |  await loudness.setVolume(numValue)
        |  testValue.finished()
        |}
        |setV(testValue.volumeValue)
        |""".stripMargin

    def execAction: Unit =
      nodeRuntime.getExecutor(execString).executeVoid()
      nodeRuntime.await()
    end execAction

    val promise             = Promise[Done]
    val future              = promise.future
    val finishedIO: F[Done] = Async[F].fromFuture(Sync[F].delay(future))

    def objResource(v8Obj: V8ValueObject): Resource[F, HaveATest] = Resource.make(
      Sync[F].delay(
        ().match
          case _ =>
            val model = HaveATest(done => promise.trySuccess(done), volumeValue = 20)
            v8Obj.bind(model)
            model
      )
    )(bindedObj => Sync[F].delay(v8Obj.unbind(bindedObj)))

    def resourceAction: Resource[F, (V8ValueGlobalObject, V8ValueObject)] = for
      v8ValueObject  <- Resource.fromAutoCloseable(Sync[F].delay(nodeRuntime.createV8ValueObject()))
      globalObject   <- Resource.fromAutoCloseable(Sync[F].delay(nodeRuntime.getGlobalObject))
      haveATestModel <- objResource(v8ValueObject)
    yield globalObject -> v8ValueObject

    resourceAction.use((_, _).match
      case (globalObj, v8ValueObj) =>
        val action: F[Done] = Sync[F].delay(().match
          case _ =>
            globalObj.set("testValue", v8ValueObj)
            execAction
            Done
        )

        for
          done1: Done <- action
          done2: Done <- finishedIO
        yield done2
    )
  end action

end ExecActionImpl
