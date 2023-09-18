package test01.service

import cats._
import cats.effect._
import cats.implicits._
import com.caoccao.javet.annotations.V8Function
import com.caoccao.javet.interop.NodeRuntime
import com.caoccao.javet.values.V8Value
import com.caoccao.javet.values.primitive.V8ValueInteger
import com.caoccao.javet.values.reference.V8ValueObject
import net.scalax.ScalaxDone

import scala.concurrent.Promise

case class SetVolumeFinished(volume: Int)

class HaveATest(catchFunc: SetVolumeFinished => Unit) {

  @V8Function(name = "finished")
  def finished(volume: Int): Unit = {
    println("喵呜呜完成啦lalalalala")
    println(volume)
    catchFunc(SetVolumeFinished(volume = volume))
  }

}

class SetVolumeService(implicit nodeRuntime: NodeRuntime, setVolCusFunction: SetVolCusFunction) {

  def setVolume[F[_]: Async](volume: Int): F[SetVolumeFinished] = new SetVolumeServiceImpl(implicitly, implicitly).action(volume)

}

class SetVolumeServiceImpl(nodeRuntime: NodeRuntime, toFunction: SetVolCusFunction) {

  def action[F[_]: Async](volume: Int): F[SetVolumeFinished] = {

    val promise                          = Promise[SetVolumeFinished]
    val future                           = promise.future
    val finishedIO: F[SetVolumeFinished] = Async[F].fromFuture(Sync[F].delay(future))

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
        toFunction.func.callVoid(null, v8Int, v8Obj)
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
