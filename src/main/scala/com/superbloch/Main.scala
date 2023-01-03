package com.superbloch.watcher

import scala.concurrent.duration.*

import cats.syntax.all.*
import cats.effect.{Async, ExitCode, IO, IOApp, Resource}
import cats.effect.std.{Console, Queue}
import cats.effect.IO.consoleForIO
import cats.effect.IO.asyncForIO

import fs2.concurrent.Topic
import fs2.{Pipe, Stream}

import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.{Router, Server}

import WSEndpoints.wsRoute
import APIEndpoints.apiV1Routes
import DocsEndpoints.docsRoutes

object Main extends IOApp:

  override def run(args: List[String]): IO[ExitCode] =
    val port = sys.env.get("http.port").map(_.toInt).getOrElse(8080)
    new CombinedStream[IO].buildApp().compile.drain.as(ExitCode.Success)

class CombinedStream[F[_]: Async: Console] {

  def makeServerStream[F[_]: Async: Console](
      queue: Queue[F, Option[Input]],
      topic: Topic[F, Output]
  ): Stream[F, ExitCode] =
    // ): Resource[F, Server] =
    BlazeServerBuilder[F]
      .bindHttp(8080, "0.0.0.0")
      .withHttpWebSocketApp(wsb =>
        Router(
          "/ws" -> BlazeWS[F](queue, topic).routes(wsb),
          // "/api/v1" -> apiV1Routes,
          // "/ws/v1"  -> wsRoute(wsb),
          // "/"       -> docsRoutes,
        ).orNotFound
      )
      .serve

  def buildApp[F[_]: Async: Console](): Stream[F, Unit] = {
    for {
      queue <- Stream.eval(Queue.unbounded[F, Option[Input]])
      topic <- Stream.eval(Topic[F, Output])
      _ <- {
        val serverStream = makeServerStream[F](queue, topic)
        val processingStream =
          Stream
            .fromQueueNoneTerminated(queue)
            .map(in =>
              in match
                case InText(value) => OutText(value)
                case InQuit        => OutQuit
            )
            .through(topic.publish)

        val anyInput =
          Stream
            .eval(Console[F].readLine)
            .map(in => {
              Console[F].println(s"You got an input ${in}")
              true
            })

        println("Press ANY key to stop ...")

        Stream(serverStream, processingStream).parJoinUnbounded
          .interruptWhen(anyInput)
      }
    } yield ()
  }

}
