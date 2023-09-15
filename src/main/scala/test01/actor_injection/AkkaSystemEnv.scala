package bb.cc

import cats.*
import cats.implicits.given
import cats.effect.*
import com.caoccao.javet.annotations.{V8Function, V8Property}
import com.caoccao.javet.interop.NodeRuntime
import com.caoccao.javet.utils.JavetOSUtils
import com.caoccao.javet.interop.engine.{IJavetEngine, IJavetEnginePool, JavetEnginePool}
import com.caoccao.javet.enums.JSRuntimeType
import com.caoccao.javet.node.modules.NodeModuleModule
import com.caoccao.javet.values.reference.{V8ValueGlobalObject, V8ValueObject}
import org.apache.pekko.Done
import org.apache.pekko.actor.typed.ActorSystem

import java.io.File
import scala.concurrent.Promise

class ActorSystemResources[F[_], -T](actorSystem: F[ActorSystem[T]]):

  private def closeAction[UF[_]: Async, U](actorSys: ActorSystem[U]): UF[Done] = for
    unitDone              <- Sync[UF].delay(actorSys.terminate())
    closeActionDone: Done <- Async[UF].fromFuture(Sync[UF].delay(actorSys.whenTerminated))
  yield closeActionDone: Done

  def resource(using Async[F]): Resource[F, ActorSystem[T]] =
    Resource.make(actorSystem)(sys => for doneToUnit: Done <- closeAction(sys) yield doneToUnit)

end ActorSystemResources
