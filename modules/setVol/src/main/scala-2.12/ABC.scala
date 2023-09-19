package test01.service

import cats._
import cats.effect._
import cats.implicits._

import scala.concurrent.Future

object CatsCompat {

  type CompatContextShift[F[_]] = ContextShiftCompat[F]

  trait ContextShiftCompat[F[_]]
  object ContextShiftCompat {
    implicit def compatImplicit[F[_]]: ContextShiftCompat[F] = new ContextShiftCompat[F] {
      //
    }
  }

  def asyncFromFuture[F[_], T](fa: F[Future[T]])(implicit F: Async[F]): F[T] = Async[F].fromFuture(fa)

}
