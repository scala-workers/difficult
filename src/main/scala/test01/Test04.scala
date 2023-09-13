package test01

import fs2.*
import cats.*
import cats.implicits.given
import cats.effect.*
import org.apache.pekko.Done

case class ListModel(l: List[Char])

class StreamDeal[F[_]: Async](stream: Stream[F, Char]):
  locally(().match
    case _ =>
      println("ss")
      println("ss")
      println("ss")
      println("ss")
  )

  val foldStream: Stream[F, ListModel] = stream.fold(ListModel(List.empty))((_, _).match
    case (m, charModel) => ListModel(charModel :: m.l)
  )
  val mapAsync: Stream[F, Done] = foldStream.map(_.l).map { u =>
    /*Sync[F].delay*/
    {
      println(u)
      Done
    }

    /*u match
      case '5' :: '3' :: '1' :: '2' :: tail => for _ <- Sync[F].delay(println("啊啊啊" * 1000)) yield Done
      case _ =>
        Sync[F].delay {
          println(u)
          Done
        }*/
  }

end StreamDeal
