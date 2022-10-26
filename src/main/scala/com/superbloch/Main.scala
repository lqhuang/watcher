package com.superbloch

import cats.effect.{ExitCode, IO, IOApp}
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router
import sttp.tapir.server.http4s.Http4sServerInterpreter

import scala.io.StdIn

object Main extends IOApp:

  override def run(args: List[String]): IO[ExitCode] =

    val routes = Http4sServerInterpreter[IO]().toRoutes(Endpoints.all)

    val port = sys.env.get("http.port").map(_.toInt).getOrElse(8080)

    BlazeServerBuilder[IO]
      .bindHttp(port, "0.0.0.0")
      .withHttpApp(Router("/" -> routes).orNotFound)
      .resource
      .use { server =>
        IO.blocking {
          println(
            s"Go to http://localhost:${server.address.getPort}/docs to open SwaggerUI. Press ENTER key to exit."
          )
          StdIn.readLine()
        }
      }
      .as(ExitCode.Success)
