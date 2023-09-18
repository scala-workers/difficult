package test01.node_runtime

import cats._
import cats.effect._
import cats.implicits._
import com.caoccao.javet.interop.NodeRuntime
import com.caoccao.javet.interop.engine.{IJavetEngine, IJavetEnginePool}

class JavetEngineWrap(bb: IJavetEnginePool[NodeRuntime]) {
  def resource[F[_]: Sync]: Resource[F, IJavetEngine[NodeRuntime]] = {
    val engineF = Sync[F].delay(bb.getEngine)
    Resource.fromAutoCloseable(engineF)
  }
}
