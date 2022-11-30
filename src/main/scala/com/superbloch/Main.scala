package com.superbloch.watcher

import scala.io.StdIn

import cats.effect.{Async, ExitCode, IO, IOApp}

import fs2.Stream

import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router

import WSEndpoints.wsRoute
import APIEndpoints.apiV1Routes
import DocsEndpoints.docsRoutes

object Main extends IOApp:

  override def run(args: List[String]): IO[ExitCode] =

    val port = sys.env.get("http.port").map(_.toInt).getOrElse(8080)

    BlazeServerBuilder[IO]
      .bindHttp(port, "0.0.0.0")
      .withHttpWebSocketApp(wsb =>
        Router(
          "/api/v1"  -> apiV1Routes,
          "/ws/v1"   -> wsRoute(wsb),
          "/ws/test" -> BlazeWS[IO].routes(wsb),
          "/"        -> docsRoutes,
        ).orNotFound
      )
      .resource
      .use { server =>
        IO.blocking {
          println(
            s"Go to http://localhost:${server.address.getPort}/api/docs to open SwaggerUI. Press ENTER key to exit."
          )
          StdIn.readLine()
        }
      }
      .as(ExitCode.Success)

object ServerStream {
  def serverStream[F[_]: Async]: Stream[F, ExitCode] =
    BlazeServerBuilder[F]
      .bindHttp(8080, "0.0.0.0")
      .withHttpWebSocketApp(wsb =>
        Router(
          "/ws" -> BlazeWS[F].routes(wsb)
        ).orNotFound
      )
      .serve
}
