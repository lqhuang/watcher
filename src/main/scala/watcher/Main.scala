/*
 * Copyright 2024 Lanqing Huang
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package io.lqhuang
package watcher

import cats.syntax.all.*
import cats.effect.{Async, ExitCode, IO, IOApp}
import cats.effect.std.{AtomicCell, Console}
import cats.effect.IO.{asyncForIO, consoleForIO}

import fs2.Stream

import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router

import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory
import org.typelevel.log4cats.extras.LogLevel

import types.WTopic

object Main extends IOApp:

    given Async[IO]         = asyncForIO
    given Console[IO]       = consoleForIO
    given LoggerFactory[IO] = Slf4jFactory.create[IO]
    given LogLevel          = LogLevel.Info

    override def run(args: List[String]): IO[ExitCode] =
        val host =
          sys.env.get("WATCHER_HOST").map(_.toString).getOrElse("0.0.0.0")
        val port = sys.env.get("WATCHER_PORT").map(_.toInt).getOrElse(8080)
        new StreamBuilder[IO]
          .buildApp(host, port)
          .compile
          .drain
          .as(ExitCode.Success)

class StreamBuilder[F[_]: Async: Console: LoggerFactory]:
    def buildApp[F[_]: Async: Console: LoggerFactory](
      host: String,
      port: Int
    ): Stream[F, Unit] = {
      val logger = LoggerFactory[F].getLogger

      for {
        topicMap <- Stream.eval(
          AtomicCell[F].of(Map.empty[String, WTopic[F]])
        )
        _ <- Stream.eval(logger.info("Starting watcher application"))
        _ <- {
          val server = BlazeServerBuilder[F]
            .bindHttp(port, host)
            .withHttpWebSocketApp(wsb =>
              Router(
                "/" -> WSRoute[F](topicMap).routes(wsb),
                // "/api/v1" -> apiV1Routes,
                // "/"       -> docsRoutes,
              ).orNotFound
            )
            .serve

          Stream(server).parJoinUnbounded
        }
      } yield ()
    }
