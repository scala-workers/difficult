package test01

import fs2.*
import cats.*
import cats.implicits.given
import cats.effect.*
import org.apache.pekko.Done
import test01.service.SetVolumeService

case class ListModel(l: List[Char])

class StreamDeal[F[_]: Async](stream: Stream[F, Char], setVolumeService: SetVolumeService[F]):

  val foldStream: Stream[F, ListModel] = stream
    .mapAccumulate(ListModel(List.empty))((_, _).match
      case (listModel, charModel) =>
        val tempModel = ListModel(charModel :: listModel.l)
        tempModel -> Done
    )
    .map(_._1)

  val mapAsync: Stream[F, Done] = foldStream.mapAsync(1)(_.l.match
    case '2' :: '1' :: '3' :: '5' :: tail =>
      Sync[F].delay(() match
        case _ =>
          println("触发热键" * 100)
          Done
      )
      setVolumeService.setVolume
    case u =>
      for (_ <- Sync[F].delay(println(u))) yield Done
  )

end StreamDeal
