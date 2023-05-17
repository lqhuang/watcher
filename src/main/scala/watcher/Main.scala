/*
 * Copyright 2022 Lanqing Huang
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package io.lqhuang
package watcher

import java.time.Instant
import scala.concurrent.duration.*
import scala.language.unsafeNulls

import cats.syntax.all.*
import cats.effect.{Async, ExitCode, IO, IOApp, Resource}
import cats.effect.std.{Console, Queue}
import cats.effect.IO.{asyncForIO, consoleForIO}

import fs2.{Pipe, Stream}
import fs2.concurrent.Topic
// import fs2.Compiler.Target.forConcurrent

import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.{Router, Server}

import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.{loggerFactoryforSync, Slf4jFactory}

import data.*

// import APIEndpoints.apiV1Routes
// import DocsEndpoints.docsRoutes

object Main extends IOApp:

  given Async[IO]         = asyncForIO
  given Console[IO]       = consoleForIO
  given LoggerFactory[IO] = Slf4jFactory[IO]

  override def run(args: List[String]): IO[ExitCode] =
    val port = sys.env.get("http.port").map(_.toInt).getOrElse(8080)
    new CombinedStream[IO].buildApp().compile.drain.as(ExitCode.Success)

class CombinedStream[F[_]: Async: Console: LoggerFactory] {
  def makeServerStream[F[_]: Async: Console: LoggerFactory](
      queue: Queue[F, Option[Input]],
      topic: Topic[F, Output],
  ): Stream[F, ExitCode] =
    BlazeServerBuilder[F]
      .bindHttp(8080, "0.0.0.0")
      .withHttpWebSocketApp(wsb =>
        Router(
          "/ws" -> BlazeWS[F](queue, topic).routes(wsb),
          // "/api/v1" -> apiV1Routes,
          // "/"       -> docsRoutes,
        ).orNotFound
      )
      .serve

  def buildApp[F[_]: Async: Console: LoggerFactory](): Stream[F, Unit] =
    for {
      queue <- Stream.eval(Queue.unbounded[F, Option[Input]])
      topic <- Stream.eval(Topic[F, Output])
      _ <- {
        val serverStream = makeServerStream[F](queue, topic)
        val processingStream =
          Stream
            .fromQueueNoneTerminated(queue)
            .map { in =>
              in match
                case InText(value) => OutText(value)
                case InEvent(id, name, eventTime, payload) =>
                  OutEvent(id, name, eventTime, Instant.now(), payload)
            }
            .through(topic.publish)

        /**
         * ANY Key interruption may not work normally in docker environment
         */
        // val anyInput =
        //   Stream
        //     .eval(Console[F].readLine)
        //     .map(in => {
        //       Console[F].println(s"You got an input ${in}")
        //       true
        //     })

        // println("Press ANY key to stop ...")
        Stream(serverStream, processingStream).parJoinUnbounded
        // .interruptWhen(anyInput)
      }
    } yield ()

}
