package com.superbloch

import scala.concurrent.duration.*

import cats.effect.IO
import fs2.{Stream, Pipe}
import sttp.capabilities.WebSockets
import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir.CodecFormat
import sttp.tapir.{endpoint, webSocketBody}
import sttp.tapir.server.http4s.Http4sServerInterpreter

// import sttp.tapir.json.jsoniter.*
// import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker

import sttp.tapir.Codec
import sttp.ws.WebSocketFrame.Ping

object WSEndpoints {

  val prefix = "/ws/v1"

  // // using Codec
  // given Codec[String, Unit, CodecFormat.TextPlain] {
  //   def raw
  // }

  val wsEndpoint =
    endpoint.get
      .out(
        webSocketBody[
          String,
          CodecFormat.TextPlain,
          String,
          CodecFormat.TextPlain
        ](
          Fs2Streams[IO]
        )
      )

  val broadcastToConnected: IO[Stream[IO, String] => Stream[IO, String]] =
    IO.pure { _ =>
      val seconds = Stream.awakeEvery[IO](1.seconds)
      seconds.map(x =>
        s"Hello, You're connected, got msg at ${x.toSeconds} seconds"
      )
    }

  val wsSrvEndpoint =
    wsEndpoint.serverLogicSuccess[IO](_ => broadcastToConnected)

  val wsRoute =
    Http4sServerInterpreter[IO]().toWebSocketRoutes(wsSrvEndpoint)
}
