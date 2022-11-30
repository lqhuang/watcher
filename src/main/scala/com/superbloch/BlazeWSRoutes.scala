package com.superbloch.watcher

import scala.concurrent.duration.*

import cats.syntax.all.*
import cats.effect.Async
import cats.effect.std.Queue
import fs2.{Pipe, Stream}
import fs2.concurrent.Topic

import org.http4s.dsl.Http4sDsl
import org.http4s.implicits.*
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame.*
import org.http4s.HttpRoutes

sealed trait Input
case class InText(value: String) extends Input
case object InQuit               extends Input

sealed trait Output
case class OutText(value: String) extends Output
case object OutQuit               extends Output

class BlazeWS[F[_]](using F: Async[F]) extends Http4sDsl[F] {

  val queue = Stream.eval(Queue.unbounded[F, Option[Input]])
  val topic = Stream.eval(Topic[F, Output])

  def routes(wsb: WebSocketBuilder2[F]): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root =>
        Ok("You got me :)")

      case GET -> Root / "send" / channel => {

        for {
          t <- topic
          p <- t.publish1(OutText("Got a GET request"))
        } yield ()

        Ok(s"Successfully send a GET request to ${channel}")
      }

      case POST -> Root / "send" / channel =>
        Ok(s"Successful ping ${channel}")

      // alive route
      case GET -> Root / "ws" => {
        val toClient: Stream[F, WebSocketFrame] =
          Stream.awakeEvery[F](2.seconds).map(d => Text(s"Hello! ${d}"))
        val fromClient: Pipe[F, WebSocketFrame, Unit] = _.evalMap {
          case Text(s, _) => F.delay(println(s))
          case f          => F.delay(println(s"Unknown type: $f"))
        }
        wsb.build(toClient, fromClient)
      }

      case GET -> Root / "ws" / channel => {
        val fromClient: Pipe[F, WebSocketFrame, Unit] = _.evalMap {
          case Text(s, _) => F.delay(println(s))
          case _ =>
            F.unit
        }
        val toClient: Stream[F, WebSocketFrame] =
          topic.flatMap(t =>
            t.subscribe(10)
              .map(
                _ match
                  case OutText(value) => Text(value)
                  case OutQuit        => Close()
              )
          )
        wsb.build(toClient, fromClient)
      }
    }
}

object BlazeWS {
  def apply[F[_]: Async]: BlazeWS[F] =
    new BlazeWS[F]
}
