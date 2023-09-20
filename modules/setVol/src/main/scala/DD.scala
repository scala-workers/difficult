package test01.service

import cats._
import cats.effect._
import cats.implicits._
import com.caoccao.javet.interop.NodeRuntime
import com.caoccao.javet.values.reference.{V8ValueFunction, V8ValueObject}
import net.scalax.ScalaxDone

case class SetVolCusFunction(setVolumeAction: V8ValueFunction)

class ToFunction(nodeRuntime: NodeRuntime) {

  val execString: String =
    """
      |const loudness = require('loudness')
      |async function setVolumeAction(numValue, asyncCatchObj) {
      |  await loudness.setVolume(numValue);
      |  asyncCatchObj.setVolumeFinished(true);
      |}
      |async function getVolumeAction(asyncCatchObj) {
      |  const vValue = await loudness.getVolume();
      |  asyncCatchObj.getVolumeFinished(vValue);
      |}
      |const exportObject = {
      |  setVolumeAction: setVolumeAction,
      |  getVolumeAction: getVolumeAction
      |}
      |""".stripMargin

  def resource[F[_]: Sync]: Resource[F, SetVolCusFunction] = {
    val preAction = Sync[F].delay {
      nodeRuntime.getExecutor(execString).executeVoid
    }

    val functionAction = Sync[F].delay {
      nodeRuntime.getExecutor("exportObject; ").execute[V8ValueObject]
    }

    val resourceAction = Resource.fromAutoCloseable(functionAction)

    def setVolumeActionF(v8Obj: V8ValueObject): F[V8ValueFunction] = Sync[F].delay {
      v8Obj.getProperty[V8ValueFunction]("setVolumeAction")
    }
    def resourcesetVolumeActionF(v8Obj: V8ValueObject): Resource[F, V8ValueFunction] = Resource.fromAutoCloseable(setVolumeActionF(v8Obj))

    val liftK = Resource.liftK[F]
    for {
      unitAction <- liftK(preAction)
      re         <- resourceAction
      setFuncRe  <- resourcesetVolumeActionF(re)
    } yield SetVolCusFunction(setVolumeAction = setFuncRe)
  }

}
