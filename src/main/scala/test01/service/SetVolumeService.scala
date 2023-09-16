package test01.service

import cats.*
import cats.effect.*
import cats.implicits.given
import com.caoccao.javet.annotations.{V8Function, V8Property}
import com.caoccao.javet.interop.NodeRuntime
import com.caoccao.javet.values.reference.{V8ValueGlobalObject, V8ValueObject}
import org.apache.pekko.Done

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

class SetVolumeService[F[_]: Async](nodeRuntimeResource: Resource[F, NodeRuntime]):

  def setVolume: F[Done] = nodeRuntimeResource.use(runtime => SetVolumeServiceImpl(runtime).action)

end SetVolumeService

class SetVolumeServiceImpl(nodeRuntime: NodeRuntime):

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

end SetVolumeServiceImpl
