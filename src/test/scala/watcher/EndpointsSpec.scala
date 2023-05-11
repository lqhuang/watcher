/*
 * Copyright 2022 Lanqing Huang
 *
 * SPDX-License-Identifier: Apache-2.0
 */
// package io.lqhuang.watcher

// import watcher.*

// import org.scalatest.EitherValues
// import org.scalatest.flatspec.AnyFlatSpec
// import org.scalatest.matchers.should.Matchers
// import sttp.client3.testing.SttpBackendStub
// import sttp.client3.{basicRequest, UriContext}
// import sttp.tapir.server.stub.TapirStubInterpreter

// import cats.effect.IO
// import cats.effect.unsafe.implicits.global
// import sttp.client3.jsoniter.*
// import sttp.tapir.integ.cats.CatsMonadError

// class EndpointsSpec extends AnyFlatSpec with Matchers with EitherValues:

//   it should "return hello message" in {
//     // given
//     val backendStub =
//       TapirStubInterpreter(SttpBackendStub(new CatsMonadError[IO]()))
//         .whenServerEndpoint(helloServerEndpoint)
//         .thenRunLogic()
//         .backend()

//     // when
//     val response = basicRequest
//       .get(uri"http://test.com/hello?name=adam")
//       .send(backendStub)

//     // then
//     response.map(_.body.value shouldBe "Hello adam").unwrap
//   }

//   it should "list available books" in {
//     // given
//     val backendStub =
//       TapirStubInterpreter(SttpBackendStub(new CatsMonadError[IO]()))
//         .whenServerEndpoint(booksListingServerEndpoint)
//         .thenRunLogic()
//         .backend()

//     // when
//     val response = basicRequest
//       .get(uri"http://test.com/books/list/all")
//       .response(asJson[List[Book]])
//       .send(backendStub)

//     // then
//     response.map(_.body.value shouldBe books).unwrap
//   }

//   extension [T](t: IO[T]) def unwrap: T = t.unsafeRunSync()
