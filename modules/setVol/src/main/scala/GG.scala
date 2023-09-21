package test01.service

import cats._
import cats.effect._
import cats.implicits._
import com.caoccao.javet.annotations.V8Function
import com.caoccao.javet.interop.NodeRuntime
import com.caoccao.javet.values.reference.{V8ValueFunction, V8ValueObject}
import net.scalax.ScalaxDone

import scala.concurrent.Promise

case class GetMutedFinished(isMuted: Boolean)

class HaveATest5(catchFunc: GetMutedFinished => Unit) {

  @V8Function(name = "getMutedFinished")
  def getMutedFinished(isMuted: Boolean): Unit = {
    catchFunc(GetMutedFinished(isMuted = isMuted))
  }

}

class GetMutedService(implicit nodeRuntime: NodeRuntime, setVolumeFinished: SetVolCusFunction) {

  def getMuted[F[_]: Async: CatsCompat.CompatContextShift]: F[GetMutedFinished] =
    new GetMutedServiceImpl(implicitly, getMutedFunction = setVolumeFinished.getVolumeAction).action

}

class GetMutedServiceImpl(nodeRuntime: NodeRuntime, getMutedFunction: V8ValueFunction) {

  def action[F[_]: Async: CatsCompat.CompatContextShift]: F[GetMutedFinished] = {

    val promise                         = Promise[GetMutedFinished]
    val future                          = promise.future
    val finishedIO: F[GetMutedFinished] = CatsCompat.asyncFromFuture[F, GetMutedFinished](Sync[F].delay(future))

    val bindedJSModel = Sync[F].delay {
      val v8Obj = nodeRuntime.createV8ValueObject
      val model = new HaveATest5(finished => promise.trySuccess(finished))
      v8Obj.bind(model)
      v8Obj
    }
    val resourceAction: Resource[F, V8ValueObject] = Resource.fromAutoCloseable(bindedJSModel)

    def modelAction(v8Obj: V8ValueObject): F[GetMutedFinished] = {
      val execNode = Sync[F].delay {
        getMutedFunction.callVoid(null, v8Obj)
        nodeRuntime.await
      }

      for {
        action: Boolean <- execNode
        finished        <- finishedIO
      } yield finished
    }

    val rSum = for {
      r <- resourceAction
    } yield r

    rSum.use(t => modelAction(t))

  }

}
