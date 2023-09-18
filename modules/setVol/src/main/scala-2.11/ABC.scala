package test01.service

import cats._
import cats.effect._
import cats.implicits._

object ResourceImpl {

  def FToResourceF[F[_]](applicative: Applicative[F]): F ~> ({ type FA[B] = Resource[F, B] })#FA = Resource.liftK

}
