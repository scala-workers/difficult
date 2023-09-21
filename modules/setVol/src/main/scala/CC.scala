package test01.service

import cats._
import cats.effect._
import cats.implicits._
import com.caoccao.javet.annotations.V8Function
import com.caoccao.javet.interop.NodeRuntime
import com.caoccao.javet.values.primitive.V8ValueInteger
import com.caoccao.javet.values.reference.{V8ValueFunction, V8ValueObject}
import net.scalax.ScalaxDone

import scala.concurrent.Promise

case class SetVolumeFinished(successsed: Boolean)

class HaveATest(catchFunc: SetVolumeFinished => Unit) {

  @V8Function(name = "setVolumeFinished")
  def setVolumeFinished(successed: Boolean): Unit = {
    catchFunc(SetVolumeFinished(successsed = successed))
  }

}

class SetVolumeService(implicit nodeRuntime: NodeRuntime, setVolCusFunction: SetVolCusFunction) {

  def setVolume[F[_]: Async: CatsCompat.CompatContextShift](volume: Int): F[SetVolumeFinished] =
    new SetVolumeServiceImpl(implicitly, setVolumeFunction = setVolCusFunction.setVolumeAction).action(volume)

}

class SetVolumeServiceImpl(nodeRuntime: NodeRuntime, setVolumeFunction: V8ValueFunction) {

  def action[F[_]: Async: CatsCompat.CompatContextShift](volume: Int): F[SetVolumeFinished] = {

    val promise                          = Promise[SetVolumeFinished]
    val future                           = promise.future
    val finishedIO: F[SetVolumeFinished] = CatsCompat.asyncFromFuture[F, SetVolumeFinished](Sync[F].delay(future))

    val bindedJSModel = Sync[F].delay {
      val v8Obj = nodeRuntime.createV8ValueObject
      val model = new HaveATest(finished => promise.trySuccess(finished))
      v8Obj.bind(model)
      v8Obj
    }
    val resourceAction: Resource[F, V8ValueObject] = Resource.fromAutoCloseable(bindedJSModel)
    val integerResourceAction: Resource[F, V8ValueInteger] =
      Resource.fromAutoCloseable(Sync[F].delay(nodeRuntime.createV8ValueInteger(volume)))

    def modelAction(v8Int: V8ValueInteger, v8Obj: V8ValueObject): F[SetVolumeFinished] = {
      val execNode = Sync[F].delay {
        setVolumeFunction.callVoid(null, v8Int, v8Obj)
        nodeRuntime.await
      }

      for {
        action: Boolean <- execNode
        finished        <- finishedIO
      } yield finished
    }

    val rSum = for {
      r1 <- integerResourceAction
      r2 <- resourceAction
    } yield (r1, r2)

    rSum.use(t => modelAction(t._1, t._2))

  }

}
