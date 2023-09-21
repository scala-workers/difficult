package test01.service

import cats._
import cats.effect._
import cats.implicits._
import com.caoccao.javet.annotations.V8Function
import com.caoccao.javet.interop.NodeRuntime
import com.caoccao.javet.values.primitive.V8ValueBoolean
import com.caoccao.javet.values.reference.{V8ValueFunction, V8ValueObject}
import net.scalax.ScalaxDone

import scala.concurrent.Promise

case class SetMutedFinished(succeeded: Boolean)

class HaveATest4(catchFunc: SetMutedFinished => Unit) {

  @V8Function(name = "setMutedFinished")
  def getVolumeFinished(succeeded: Boolean): Unit = {
    catchFunc(SetMutedFinished(succeeded = succeeded))
  }

}

class SetMutedService(implicit nodeRuntime: NodeRuntime, setVolumeFinished: SetVolCusFunction) {

  def setMuted[F[_]: Async: CatsCompat.CompatContextShift](needMuted: Boolean): F[SetMutedFinished] =
    Sync[F].delay(new SetMutedServiceImpl(implicitly, setMutedFunction = setVolumeFinished.setMutedAction).action(needMuted)).flatten

}

class SetMutedServiceImpl(nodeRuntime: NodeRuntime, setMutedFunction: V8ValueFunction) {

  def action[F[_]: Async: CatsCompat.CompatContextShift](needMuted: Boolean): F[SetMutedFinished] = {

    val promise                         = Promise[SetMutedFinished]
    val future                          = promise.future
    val finishedIO: F[SetMutedFinished] = CatsCompat.asyncFromFuture[F, SetMutedFinished](Sync[F].delay(future))

    val bindedJSModel = Sync[F].delay {
      val v8Obj = nodeRuntime.createV8ValueObject
      val model = new HaveATest4(finished => promise.trySuccess(finished))
      v8Obj.bind(model)
      v8Obj
    }
    val resourceAction: Resource[F, V8ValueObject] = Resource.fromAutoCloseable(bindedJSModel)
    val booleanResourceAction: Resource[F, V8ValueBoolean] =
      Resource.fromAutoCloseable(Sync[F].delay(nodeRuntime.createV8ValueBoolean(needMuted)))

    def modelAction(v8Boolean: V8ValueBoolean, v8Obj: V8ValueObject): F[SetMutedFinished] = {
      val execNode = Sync[F].delay {
        setMutedFunction.callVoid(null, v8Boolean, v8Obj)
        nodeRuntime.await
      }

      for {
        action: Boolean <- execNode
        finished        <- finishedIO
      } yield finished
    }

    val rSum = for {
      r1 <- booleanResourceAction
      r2 <- resourceAction
    } yield (r1, r2)

    rSum.use(t => modelAction(t._1, t._2))

  }

}
