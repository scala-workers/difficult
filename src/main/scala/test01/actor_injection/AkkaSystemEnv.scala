package bb.cc

import cats.*
import cats.implicits.given
import cats.effect.*
import org.apache.pekko.Done
import org.apache.pekko.actor.typed.ActorSystem

class ActorSystemResources[F[_], -T](actorSystem: F[ActorSystem[T]]):

  private def closeAction[UF[_]: Async, U](actorSys: ActorSystem[U]): UF[Done] = for
    unitDone              <- Sync[UF].delay(actorSys.terminate())
    closeActionDone: Done <- Async[UF].fromFuture(Sync[UF].delay(actorSys.whenTerminated))
  yield closeActionDone: Done

  def resource(using Async[F]): Resource[F, ActorSystem[T]] =
    Resource.make(actorSystem)(sys => for doneToUnit: Done <- closeAction(sys) yield doneToUnit)

end ActorSystemResources
