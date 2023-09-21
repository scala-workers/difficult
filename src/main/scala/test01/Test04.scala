package test01

import fs2.*
import cats.*
import cats.implicits.given
import cats.effect.*
import org.apache.pekko.Done
import test01.node_runtime.LoudnessService
import test01.service.{SetMutedFinished, SetVolumeFinished}

case class ListModel(l: List[Char])

class StreamDeal[F[_]: Async](stream: Stream[F, Char], setVolumeService: LoudnessService[F]):

  val changeMutedAction: F[SetMutedFinished] = for
    isMuted                    <- setVolumeService.getMuted
    finished: SetMutedFinished <- setVolumeService.setMuted(!isMuted.isMuted)
  yield finished

  def up10(in: Int): Int = {
    val n     = BigDecimal(in) * BigDecimal("1.10")
    val tempN = n.toInt
    if (tempN - in > 5) tempN else math.min(in + 5, 100)
  }
  val UpdateVol: F[SetVolumeFinished] = for
    volResoult                  <- setVolumeService.getVolume
    finished: SetVolumeFinished <- setVolumeService.setVolume(up10(volResoult.volume))
  yield finished

  def down10(in: Int): Int = {
    val n     = BigDecimal(in) / BigDecimal("1.10")
    val tempN = n.toInt
    if (in - tempN > 5) tempN else math.max(in - 5, 0)
  }
  val DownVol: F[SetVolumeFinished] = for
    volResoult                  <- setVolumeService.getVolume
    finished: SetVolumeFinished <- setVolumeService.setVolume(down10(volResoult.volume))
  yield finished

  val foldStream: Stream[F, ListModel] = stream
    .mapAccumulate(ListModel(List.empty))((_, _).match
      case (listModel, charModel) =>
        val tempModel = ListModel(charModel :: listModel.l)
        tempModel -> Done
    )
    .map(_._1)

  val mapAsync: Stream[F, Done] = foldStream.mapAsync(1)(_.l.match
    case '2' :: '1' :: '3' :: '5' :: tail =>
      println("触发切换静音/非静音")
      for _: SetMutedFinished <- changeMutedAction yield Done

    case '3' :: '1' :: '3' :: '5' :: tail =>
      println("触发提升音量")
      for _: SetVolumeFinished <- UpdateVol yield Done

    case '8' :: '2' :: '3' :: '5' :: tail =>
      println("触发降低音量")
      for _: SetVolumeFinished <- DownVol yield Done

    case u =>
      for (_ <- Sync[F].delay(println(u))) yield Done
  )

end StreamDeal
