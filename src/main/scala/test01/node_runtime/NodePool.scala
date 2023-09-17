package test01.node_runtime

import cats.*
import cats.effect.*
import cats.implicits.given
import com.caoccao.javet.enums.JSRuntimeType
import com.caoccao.javet.interop.NodeRuntime
import com.caoccao.javet.interop.engine.{IJavetEngine, IJavetEnginePool, JavetEnginePool}

class JavetEngineWrap(bb: IJavetEnginePool[NodeRuntime]):
  def resource[F[_]: Sync]: Resource[F, IJavetEngine[NodeRuntime]] = Resource.fromAutoCloseable(Sync[F].delay(bb.getEngine))
end JavetEngineWrap
