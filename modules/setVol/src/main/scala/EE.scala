package test01.service

import cats._
import cats.effect._
import cats.implicits._
import com.caoccao.javet.annotations.V8Function
import com.caoccao.javet.interop.NodeRuntime
import com.caoccao.javet.values.reference.{V8ValueFunction, V8ValueObject}
import net.scalax.ScalaxDone

import scala.concurrent.Promise

case class GetVolumeFinished(volume: Int)

class HaveATest3(catchFunc: GetVolumeFinished => Unit) {

  @V8Function(name = "getVolumeFinished")
  def getVolumeFinished(volume: Int): Unit = {
    catchFunc(GetVolumeFinished(volume))
  }

}

class GetVolumeService(implicit nodeRuntime: NodeRuntime, setVolumeFinished: SetVolCusFunction) {

  def getVolume[F[_]: Async: CatsCompat.CompatContextShift]: F[GetVolumeFinished] =
    new GetVolumeServiceImpl(implicitly, getVolumeFunction = setVolumeFinished.getVolumeAction).action

}

class GetVolumeServiceImpl(nodeRuntime: NodeRuntime, getVolumeFunction: V8ValueFunction) {

  def promiseF[F[_]: Sync]: F[Promise[GetVolumeFinished]] = Sync[F].delay(Promise[GetVolumeFinished])

  def action[F[_]: Async: CatsCompat.CompatContextShift]: F[GetVolumeFinished] = promiseF.flatMap { promise =>

    val future                           = promise.future
    val finishedIO: F[GetVolumeFinished] = CatsCompat.asyncFromFuture[F, GetVolumeFinished](Sync[F].delay(future))

    val bindedJSModel = Sync[F].delay {
      val v8Obj = nodeRuntime.createV8ValueObject
      val model = new HaveATest3(finished => promise.trySuccess(finished))
      v8Obj.bind(model)
      v8Obj
    }
    val resourceAction: Resource[F, V8ValueObject] = Resource.fromAutoCloseable(bindedJSModel)

    def modelAction(v8Obj: V8ValueObject): F[GetVolumeFinished] = {
      val execNode = Sync[F].delay {
        getVolumeFunction.callVoid(null, v8Obj)
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
