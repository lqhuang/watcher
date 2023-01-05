package io.lqhuang
package watcher

import java.time.Instant

import io.circe.generic.semiauto.{deriveCodec, deriveDecoder, deriveEncoder}
import io.circe.{Codec, Decoder, Encoder, Json}
import io.circe.syntax.*
// import io.circe.generic.JsonCodec // ???

// "-Yexplicit-nulls" is only available after:
// https://github.com/circe/circe/issues/1786
// https://github.com/circe/circe/pull/1788

sealed trait Input

final case class InText(value: String) extends Input

final case class InEvent(
    id: Int,
    name: String,
    eventTime: Instant,
    payload: Json
) extends Input
  derives Codec.AsObject

// given Codec[InEvent] = deriveCodec[InEvent]

sealed trait Output
final case class OutText(value: String) extends Output

final case class OutEvent(
    id: Int,
    name: String,
    eventTime: Instant,
    arrivalTime: Instant,
    payload: Json,
) extends Output
  derives Codec.AsObject

// given Codec[OutEvent] = deriveCodec[OutEvent]
