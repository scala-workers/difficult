package test01.node_runtime

import cats._
import cats.effect._
import cats.implicits._
import com.caoccao.javet.interop.NodeRuntime
import test01.service.{
  CatsCompat,
  GetMutedFinished,
  GetMutedService,
  GetVolumeFinished,
  GetVolumeService,
  SetMutedFinished,
  SetMutedService,
  SetVolCusFunction,
  SetVolumeFinished,
  SetVolumeService,
  ToFunction
}

trait LoudnessService[F[_]] {
  def setVolume(volume: Int): F[SetVolumeFinished]
  def getVolume: F[GetVolumeFinished]
  def setMuted(muted: Boolean): F[SetMutedFinished]
  def getMuted: F[GetMutedFinished]
}

abstract class LoudnessServiceImpl1[F[_]: Async: CatsCompat.CompatContextShift] extends LoudnessService[F] {
  def setVolService: SetVolumeService
  def getVolService: GetVolumeService
  def setMutedService: SetMutedService
  def getMutedService: GetMutedService

  override def setVolume(volume: Int): F[SetVolumeFinished]  = setVolService.setVolume(volume)
  override def getVolume: F[GetVolumeFinished]               = getVolService.getVolume
  override def setMuted(muted: Boolean): F[SetMutedFinished] = setMutedService.setMuted(muted)
  override def getMuted: F[GetMutedFinished]                 = getMutedService.getMuted
}

abstract class LoudnessServiceImpl2[F[_]: Async: CatsCompat.CompatContextShift] extends LoudnessServiceImpl1[F] {
  implicit def nodeRuntime: NodeRuntime
  implicit def setFunction: SetVolCusFunction

  override def setVolService: SetVolumeService  = new SetVolumeService()(nodeRuntime = implicitly, setVolCusFunction = implicitly)
  override def getVolService: GetVolumeService  = new GetVolumeService()(nodeRuntime = implicitly, setVolumeFinished = implicitly)
  override def setMutedService: SetMutedService = new SetMutedService()(nodeRuntime = implicitly, setVolumeFinished = implicitly)
  override def getMutedService: GetMutedService = new GetMutedService()(nodeRuntime = implicitly, setVolumeFinished = implicitly)
}

class LoudnessServiceImpl3[F[_]: Async: CatsCompat.CompatContextShift](
  override val nodeRuntime: NodeRuntime,
  override val setFunction: SetVolCusFunction
) extends LoudnessServiceImpl2[F]

class LoudnessServiceImpl4F(nodeRuntime: NodeRuntime) {
  def resource[F[_]: Async: CatsCompat.CompatContextShift]: Resource[F, LoudnessService[F]] =
    for (resource1: SetVolCusFunction <- new ToFunction(nodeRuntime).resource[F])
      yield new LoudnessServiceImpl3[F](nodeRuntime = nodeRuntime, setFunction = resource1)
}
