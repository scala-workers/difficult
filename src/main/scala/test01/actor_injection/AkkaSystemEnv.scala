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
    ResourceImpl.makeFunction[Done].make(actorSystem)(closeAction)

end ActorSystemResources

object ResourceImpl {
  def makeFunction[U]: MakeImpl[U] = MakeImpl[U]
  class MakeImpl[U] {
    def make[F[_], A](acquire: F[A])(release: A => F[U])(implicit FImplicit: Functor[F]): Resource[F, A] =
      Resource.make[F, A](acquire)(tModel => for u: U <- release(tModel) yield u)
  }
}
