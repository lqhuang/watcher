package com.superbloch

import cats.effect.IO
import sttp.tapir.docs.asyncapi.AsyncAPIInterpreter
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter

object DocsEndpoints {
  val asyncDocs =
    AsyncAPIInterpreter()
      .toAsyncAPI(WSEndpoints.wsSrvEndpoint, "Watcher Reactive API", "0.1.0")

  val docsSrvEndpoints = SwaggerInterpreter().fromEndpoints[IO](
    APIEndpoints.apiV1Endpoints,
    "Watcher REST API",
    "0.1.0"
  )
  val docsRoutes = Http4sServerInterpreter[IO]().toRoutes(docsSrvEndpoints)
}
