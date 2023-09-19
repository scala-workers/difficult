package test01.service

import cats._
import cats.effect._
import cats.implicits._

import scala.concurrent.Future

object CatsCompat {

  type CompatContextShift[F[_]] = ContextShift[F]

  def asyncFromFuture[F[_], T](fa: F[Future[T]])(implicit F: Async[F], cs: ContextShift[F]): F[T] = Async.fromFuture[F, T](fa)

}
