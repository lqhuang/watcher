/*
 * Copyright 2022 Lanqing Huang
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package io.lqhuang
package watcher.data

import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import java.time.temporal.ChronoUnit.MILLIS

import io.circe.{Codec, Encoder, Json}
import io.circe.syntax.*
// import io.circe.generic.semiauto.deriveCodec
// import io.circe.generic.JsonCodec

/**
 * "-Yexplicit-nulls" is only available after:
 * https://github.com/circe/circe/issues/1786
 * https://github.com/circe/circe/pull/1788
 */

given Encoder[Instant] =
  Encoder.encodeString.contramap[Instant](t =>
    ISO_OFFSET_DATE_TIME.nn
      .format(
        t.nn.truncatedTo(MILLIS).nn.atOffset(ZoneOffset.UTC)
      )
      .nn
  )

sealed trait Input
final case class InText(value: String) extends Input
final case class InEvent(
    id: Int,
    name: String,
    eventTime: Instant,
    payload: Json
) extends Input
  derives Codec.AsObject {
  def toOutEvent: OutEvent = OutEvent(
    id,
    name,
    eventTime,
    Instant.now().nn,
    payload
  )
}

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

case class WatcherResponse(
    msg: String
) derives Codec.AsObject

case class ChannelList(
    channels: List[String]
) derives Codec.AsObject
