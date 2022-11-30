package com.superbloch.watcher

import scala.concurrent.duration.*

import cats.effect.IO

import fs2.Stream

import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir.*
import sttp.tapir.server.http4s.Http4sServerInterpreter

object WSEndpoints {

  val wsV1Prefix = "ws" / "v1"

  val wsEndpoint =
    endpoint.get
      .out(
        webSocketBody[
          String,
          CodecFormat.TextPlain,
          String,
          CodecFormat.TextPlain,
        ](Fs2Streams[IO])
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

  val wsV1DocEndpoint = wsSrvEndpoint.endpoint.prependIn(wsV1Prefix)
}
