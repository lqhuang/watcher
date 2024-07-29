/*
 * Copyright 2024 Lanqing Huang
 *
 * SPDX-License-Identifier: Apache-2.0
 */
import java.time.Instant
import java.time.temporal.ChronoUnit.MILLIS

import io.circe.{Encoder, Json, JsonObject}
import io.circe.syntax.*
import io.circe.parser.{decode, parse}

import munit.FunSuite

import io.lqhuang.watcher.data.{InEvent, OutEvent}

class ModelSuite extends FunSuite {

  test("parsing InEvent") {
    val rawJson: String = """
    {
      "id": 13,
      "name": "test-event",
      "eventTime": "2023-01-05T08:04:55.697+00:00",
      "payload": {
        "datetime": "2023-01-05T08:04:55.697+00:00",
        "std_period": 10,
        "n_sigma": 3,
        "rsl_short": 0.9,
        "rsl_long": 1.1,
        "signal": 1
      }
    }
    """

    val inEvent = InEvent(
      13,
      "test-event",
      Instant.ofEpochMilli(1672905895697L),
      payload = Json.fromJsonObject(
        JsonObject(
          "datetime"   -> Json.fromString("2023-01-05T08:04:55.697+00:00"),
          "std_period" -> Json.fromInt(10),
          "n_sigma"    -> Json.fromInt(3),
          "rsl_short"  -> Json.fromDoubleOrNull(0.9),
          "rsl_long"   -> Json.fromDoubleOrNull(1.1),
          "signal"     -> Json.fromInt(1)
        )
      )
    )

    val result        = decode[InEvent](rawJson)
    val parsedInEvent = result.getOrElse(None)

    assert(result.isRight)
    assertEquals(parsedInEvent, inEvent)
  }

  test("InEvent asJson") {
    val rawJson: String = """
    {
      "id": 13,
      "name": "test-event",
      "eventTime": "2023-01-05T08:04:55.697Z",
      "payload": {
        "datetime": "2023-01-05T08:04:55.697Z",
        "std_period": 10,
        "n_sigma": 3,
        "rsl_short": 0.9,
        "rsl_long": 1.1,
        "signal": 1
      }
    }
    """

    val inEvent = InEvent(
      13,
      "test-event",
      Instant.ofEpochMilli(1672905895697L),
      payload = Json.fromJsonObject(
        JsonObject(
          "datetime"   -> Json.fromString("2023-01-05T08:04:55.697Z"),
          "std_period" -> Json.fromInt(10),
          "n_sigma"    -> Json.fromInt(3),
          "rsl_short"  -> Json.fromDoubleOrNull(0.9),
          "rsl_long"   -> Json.fromDoubleOrNull(1.1),
          "signal"     -> Json.fromInt(1)
        )
      )
    )

    val actual  = parse(rawJson).getOrElse(Json.Null).asJson.spaces2
    val encoded = inEvent.asJson.spaces2
    assertEquals(encoded, actual)
  }

  test("time resolution in OutEvent json") {
    val nanosInstant = Instant.now()
    val eventTime    = Instant.ofEpochMilli(1672905895697L)
    val outEvent = OutEvent(
      0,
      "test-out-event",
      eventTime,
      nanosInstant,
      Json.Null,
    )

    val decodedOutEvent =
      decode[OutEvent](outEvent.asJson.noSpaces).toOption

    assertEquals(
      eventTime,
      decodedOutEvent.map(_.eventTime).getOrElse(Instant.now())
    )
    assertEquals(
      nanosInstant.truncatedTo(MILLIS),
      decodedOutEvent.map(_.arrivalTime).getOrElse(Instant.now())
    )
  }

  test("test codec for Instant") {
    import io.lqhuang.watcher.data.given Encoder[Instant]

    val t1    = Instant.ofEpochMilli(1672905895697L)
    val tNano = Instant.now()

    assertEquals("2023-01-05T08:04:55.697Z", t1.asJson.asString.get)
    assertEquals(
      tNano.truncatedTo(MILLIS).toString(),
      tNano.asJson.asString.get
    )
  }
}
