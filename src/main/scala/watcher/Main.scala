/*
 * Copyright 2023 Lanqing Huang
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package io.lqhuang
package watcher

import java.time.Instant

import cats.syntax.all.*
import cats.effect.kernel.Ref
import cats.effect.{Async, ExitCode, IO, IOApp, Resource}
import cats.effect.std.Console
import cats.effect.IO.{asyncForIO, consoleForIO}

import fs2.Stream

import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.{Router, Server}

import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory

object Main extends IOApp:

  given Async[IO]         = asyncForIO
  given Console[IO]       = consoleForIO
  given LoggerFactory[IO] = Slf4jFactory.create[IO]

  override def run(args: List[String]): IO[ExitCode] =
    val port = sys.env.get("http.port").map(_.toInt).getOrElse(8080)
    val serverStream = BlazeServerBuilder[IO]
      .bindHttp(8080, "0.0.0.0")
      .withHttpWebSocketApp(wsb =>
        Router(
          "/" -> BlazeWS[IO]().routes(wsb),
          // "/api/v1" -> apiV1Routes,
          // "/"       -> docsRoutes,
        ).orNotFound
      )
      .serve

    Stream(serverStream).parJoinUnbounded.compile.drain
      .as(ExitCode.Success)
