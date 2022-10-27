package com.superbloch

import scala.io.StdIn

import cats.effect.{ExitCode, IO, IOApp}
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
          "/api/v1" -> apiV1Routes,
          "/ws/v1"  -> wsRoute(wsb),
          "/"       -> docsRoutes,
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
