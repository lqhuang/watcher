package io.lqhuang
package watcher

import cats.effect.IO

import sttp.tapir.*
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.Http4sServerInterpreter

// import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
// import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
// import sttp.tapir.generic.auto.*
// import sttp.tapir.json.jsoniter.*

object APIEndpoints {
  val apiV1Prefix = "api" / "v1"

  val indexEndpoint: PublicEndpoint[Unit, Unit, String, Any] =
    endpoint.get.in("").out(stringBody).tag("Receiver")
  val indexServerEndpoint =
    indexEndpoint.serverLogicSuccess(_ =>
      IO.pure("Hello! Here is Watcher 0.1.1")
    )

  // val helloEndpoint: PublicEndpoint[User, Unit, String, Any] = endpoint.get
  //   .in("hello")
  //   .in(query[User]("name"))
  //   .out(stringBody)
  // val helloServerEndpoint: ServerEndpoint[Any, IO] =
  //   helloEndpoint.serverLogicSuccess(user => IO.pure(s"Hello ${user.name}"))

  // given codecBooks: JsonValueCodec[List[Book]] = JsonCodecMaker.make
  // val booksListing: PublicEndpoint[Unit, Unit, List[Book], Any] = endpoint.get
  //   .in("books" / "list" / "all")
  //   .out(jsonBody[List[Book]])
  // val booksListingServerEndpoint: ServerEndpoint[Any, IO] =
  //   booksListing.serverLogicSuccess(_ => IO.pure(Library.books))

  val apiV1SrvEndpoints = List(indexServerEndpoint)
  val apiV1Routes =
    Http4sServerInterpreter[IO]().toRoutes(apiV1SrvEndpoints)

  val apiV1DocsEndpoints =
    apiV1SrvEndpoints.map(_.endpoint).map(_.prependIn(apiV1Prefix))
}

// object Library:
//   case class Author(name: String)
//   case class Book(title: String, year: Int, author: Author)

//   val books = List(
//     Book(
//       "The Sorrows of Young Werther",
//       1774,
//       Author("Johann Wolfgang von Goethe")
//     ),
//     Book("On the Niemen", 1888, Author("Eliza Orzeszkowa")),
//     Book("The Art of Computer Programming", 1968, Author("Donald Knuth")),
//     Book("Pharaoh", 1897, Author("Boleslaw Prus"))
//   )
