/*
 * Copyright 2024 Lanqing Huang
 *
 * SPDX-License-Identifier: Apache-2.0
 */
// package watcher

// import cats.effect.IO

// import com.dimafeng.testcontainers.GenericContainer
// import com.dimafeng.testcontainers.munit.fixtures.TestContainersFixtures

// import io.circe.Json
// import io.circe.syntax.*
// // import io.circe.generic.auto.*

// import org.http4s.Uri
// import org.http4s.blaze.client.BlazeClientBuilder
// import org.http4s.circe.CirceEntityCodec.{
//   circeEntityDecoder,
//   circeEntityEncoder
// }

// class TestContainersSuite extends munit.HttpSuite with TestContainersFixtures {

//   override def http4sMUnitClient = BlazeClientBuilder[IO].resource

//   // There is also available `ForEachContainerFixture`
//   val container = ForAllContainerFixture {
//     GenericContainer(
//       dockerImage = "ghcr.io/lqhuang/watcher",
//       exposedPorts = List(8080)
//     )
//   }
//   override def munitFixtures = List(container)

//   override def baseUri() =
//     localhost.withPort(container.container.mappedPort(8080))

//   test(GET(uri"list-channels")).alias("blank state") { response =>
//     assertEquals(response.status.code, 200)
//     val expected = Json.arr()
//     val cond     = response.as[Json].map(_ == expected)
//     assertIOBoolean(cond)
//   }
// }
