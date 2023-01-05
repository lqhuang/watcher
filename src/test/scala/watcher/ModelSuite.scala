import java.time.Instant

import io.circe.{Json, JsonObject}
import io.circe.syntax.*
import io.circe.parser.{decode, parse}
import io.circe.Encoder.*

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.Assertions.*

import io.lqhuang.watcher.{InEvent, OutEvent}

class ModelSuite extends AnyFunSuite {

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
    val parsedInEvent = result.getOrElse(null)

    assert(result.isRight)
    assertResult(parsedInEvent)(inEvent)
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

    val actual  = parse(rawJson).getOrElse(null).asJson.spaces2
    val encoded = inEvent.asJson.spaces2
    assertResult(encoded)(actual)
  }

}
