package com.superbloch

import scala.concurrent.duration.*

import cats.effect.{IO, Async}
import fs2.{Stream, Pipe}

import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.implicits.*
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame.*

object BlazeWS {

  def routes[F[_]](using F: Async[F])(
      wsb: WebSocketBuilder2[F]
  ): HttpRoutes[F] =
    HttpRoutes.of[F] { case GET -> Root / "ws" =>
      val toClient: Stream[F, WebSocketFrame] =
        Stream.awakeEvery[F](1.seconds).map(d => Text(s"Ping! $d"))
      val fromClient: Pipe[F, WebSocketFrame, Unit] = _.evalMap {
        case Text(t, _) => F.delay(println(t))
        case f          => F.delay(println(s"Unknown type: $f"))
      }
      wsb.build(toClient, fromClient)
    }
}
