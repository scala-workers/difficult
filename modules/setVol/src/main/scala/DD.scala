package test01.service

import cats._
import cats.effect._
import cats.implicits._
import com.caoccao.javet.interop.NodeRuntime
import com.caoccao.javet.values.reference.{V8ValueFunction, V8ValueObject}
import net.scalax.ScalaxDone

case class SetVolCusFunction(
  setVolumeAction: V8ValueFunction,
  getVolumeAction: V8ValueFunction,
  setMutedAction: V8ValueFunction,
  getMutedAction: V8ValueFunction
)

class ToFunction(nodeRuntime: NodeRuntime) {

  val execString: String =
    """
      |const loudness = require('loudness')
      |async function setVolumeAction(intVolValue, asyncCatchObj) {
      |  await loudness.setVolume(intVolValue);
      |  asyncCatchObj.setVolumeFinished(true);
      |}
      |async function getVolumeAction(asyncCatchObj) {
      |  const vValue = await loudness.getVolume();
      |  asyncCatchObj.getVolumeFinished(vValue);
      |}
      |async function setMutedAction(needMuted, asyncCatchObj) {
      |  await loudness.setMuted(needMuted);
      |  asyncCatchObj.setMutedFinished(true);
      |}
      |async function getMutedAction(asyncCatchObj) {
      |  const mValue = await loudness.getMuted();
      |  asyncCatchObj.getMutedFinished(mValue);
      |}
      |const exportObject = {
      |  setVolumeAction: setVolumeAction,
      |  getVolumeAction: getVolumeAction,
      |  setMutedAction: setMutedAction,
      |  getMutedAction: getMutedAction
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

    def getPropertyFunctionFromObj(v8Obj: V8ValueObject): String => Resource[F, V8ValueFunction] = { proName =>
      val funcF = Sync[F].delay {
        v8Obj.getProperty[V8ValueFunction](proName)
      }
      Resource.fromAutoCloseable(funcF)
    }

    val liftK = Resource.liftK[F]
    for {
      unitAction <- liftK(preAction)
      re         <- resourceAction
      getter = getPropertyFunctionFromObj(re)
      setVolFuncRe <- getter("setVolumeAction")
      getVolFuncRe <- getter("getVolumeAction")
      setMutedRe   <- getter("setMutedAction")
      getMutedRe   <- getter("getMutedAction")
    } yield SetVolCusFunction(
      setVolumeAction = setVolFuncRe,
      getVolumeAction = getVolFuncRe,
      setMutedAction = setMutedRe,
      getMutedAction = getMutedRe
    )
  }

}
