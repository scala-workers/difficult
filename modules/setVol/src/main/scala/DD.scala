package test01.service

import cats._
import cats.effect._
import cats.implicits._
import com.caoccao.javet.interop.NodeRuntime
import com.caoccao.javet.values.reference.V8ValueFunction

import net.scalax.ScalaxDone

case class SetVolCusFunction(func: V8ValueFunction)

class ToFunction(nodeRuntime: NodeRuntime) {

  val execString: String =
    """
      |const loudness = require('loudness')
      |async function setV(numValue, returnObj) {
      |  await loudness.setVolume(numValue);
      |  const vValue = await loudness.getVolume();
      |  returnObj.finished(vValue);
      |}
      |""".stripMargin

  def resource[F[_]: Sync]: Resource[F, SetVolCusFunction] = {
    val preAction = Sync[F].delay {
      nodeRuntime.getExecutor(execString).executeVoid
    }

    val functionAction = Sync[F].delay {
      nodeRuntime.getExecutor("setV; ").execute[V8ValueFunction]
    }

    val resourceAction = Resource.fromAutoCloseable(functionAction)

    val liftK = Resource.liftK[F]
    for {
      unitAction <- liftK(preAction)
      re         <- resourceAction
    } yield SetVolCusFunction(func = re)
  }

}
