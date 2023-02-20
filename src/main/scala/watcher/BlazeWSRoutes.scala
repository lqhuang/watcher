package io.lqhuang
package watcher

import scala.concurrent.duration.*

import cats.syntax.all.*
import cats.effect.Async
import cats.effect.std.{Console, Queue}
import fs2.{Pipe, Stream}
import fs2.concurrent.Topic
import io.circe.syntax.*

import org.http4s.dsl.Http4sDsl
import org.http4s.implicits.*
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame.{Close, Text}
import org.http4s.HttpRoutes

import data.*

class BlazeWS[F[_]](using F: Async[F], console: Console[F])(
    queue: Queue[F, Option[Input]],
    topic: Topic[F, Output],
) extends Http4sDsl[F] {

  def routes(wsb: WebSocketBuilder2[F]): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root =>
        Ok("You got me :)")

      case GET -> Root / "send" / channel =>
        for {
          _    <- queue.offer(Some(InText(s"${channel}")))
          resp <- Ok(s"Successfully send a GET request to ${channel}")
        } yield resp

      case req @ POST -> Root / "send" / channel =>
        for {
          in   <- req.as[InEvent]
          _    <- queue.offer(Some(in))
          resp <- Ok(s"Successful ping ${channel}")
        } yield resp

      // alive route
      case GET -> Root / "ws" => {
        val toClient: Stream[F, WebSocketFrame] =
          Stream
            .awakeEvery[F](2.seconds)
            .map(d => Text(s"Hello! ${d.toSeconds}"))
        val fromClient: Pipe[F, WebSocketFrame, Unit] = _.evalMap {
          case Text(s, _) => console.println(s)
          case Close(_)   => console.println("Got CLOSE")
          case f          => console.println(s"Unknown type: $f")
        }
        wsb.build(toClient, fromClient)
      }

      case GET -> Root / "ws" / channel => {
        val fromClient: Pipe[F, WebSocketFrame, Unit] = _.evalMap {
          case Text(s, _) => console.println(s)
          case _          => F.unit
        }
        val toClient: Stream[F, WebSocketFrame] =
          topic
            .subscribe(3)
            .map {
              _ match
                case OutText(value) => Text(value)
                case out: OutEvent  => Text(out.asJson.noSpaces)
            }
        wsb.build(toClient, fromClient)
      }
    }

}

object BlazeWS {
  def apply[F[_]: Async: Console](
      queue: Queue[F, Option[Input]],
      topic: Topic[F, Output],
  ): BlazeWS[F] =
    new BlazeWS[F](queue, topic)
}
