package com.superbloch.watcher

import scala.io.StdIn

import cats.syntax.all.*
import cats.effect.cps.*
import cats.effect.{Async, ExitCode, IO, IOApp, Resource}

import fs2.Stream

import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.{Router, Server}

import WSEndpoints.wsRoute
import APIEndpoints.apiV1Routes
import DocsEndpoints.docsRoutes

import cats.effect.std.Queue
import fs2.concurrent.Topic

object Main extends IOApp:
  import MergedStream.*

  override def run(args: List[String]): IO[ExitCode] = {
    val port = sys.env.get("http.port").map(_.toInt).getOrElse(8080)
    buildApp()
  }

object MergedStream {
  def makeServerStream[F[_]: Async](
      queue: Queue[F, Input],
      topic: Topic[F, Output]
  ): Resource[F, Server] =
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
      .resource
    // .serve
    // .use(server =>
    //   IO.blocking {
    //     println(
    //       s"Go to http://localhost:${server.address.getPort}/api/docs to open SwaggerUI. Press ENTER key to exit."
    //     )
    //     StdIn.readLine()
    //   }
    // )

  def buildApp[F[_]: Async](): F[ExitCode] = {
    async[F] {
      val queue = Queue.unbounded[F, Input].await
      val topic = Topic[F, Output].await

      //   val keepAlive = Stream
      //     .awakeEvery[F](30.seconds)
      //     .map(_ => KeepAlive)
      //     .through(topic.publish)
      // }

      val serverResource                     = makeServerStream[F](queue, topic)
      val forwardStream: Stream[F, ExitCode] = ???

      Stream
        .resource(serverResource)
        .parZip(Stream(forwardStream))
        .compile
        .drain
    }
  }.as(ExitCode.Success)
}
