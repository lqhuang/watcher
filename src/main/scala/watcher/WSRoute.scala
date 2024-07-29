/*
 * Copyright 2024 Lanqing Huang
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package io.lqhuang
package watcher

import cats.syntax.all.*
import cats.effect.Async
import fs2.Pipe
import fs2.concurrent.Topic

import io.circe.Json
import io.circe.JsonObject
import io.circe.syntax.*

import org.http4s.dsl.Http4sDsl
import org.http4s.circe.CirceEntityCodec.{
  circeEntityDecoder,
  circeEntityEncoder
}
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame.{Close, Text}
import org.http4s.HttpRoutes

import org.typelevel.log4cats.LoggerFactory

import data.*
import types.{Message, TopicMapRef, WTopic}

class WSRoute[F[_]: LoggerFactory](using async: Async[F])(
  topicMap: TopicMapRef[F]
) extends Http4sDsl[F] {

  val logger = LoggerFactory[F].getLoggerFromName(getClass.getName())

  def createTopic(): F[WTopic[F]] = Topic[F, Option[Message]]

  def getTopic(key: String): F[Option[WTopic[F]]] =
    topicMap.get.map(x => x.get(key))

  def routes(wsb: WebSocketBuilder2[F]): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root =>
        Ok(JsonObject("info" -> Json.fromString("You got me :)")))

      case GET -> Root / "list-channels" =>
        for {
          tm <- topicMap.get
          _ <- logger.info(
            s"Listing channels, current topicMap keys: ${tm.keys.toString()}"
          )
          resp <- Ok(tm.keys.toSeq.asJson)
        } yield resp

      case GET -> Root / "create" / channel =>
        for {
          maybeTopic <- getTopic(channel)
          existed <- maybeTopic match
              case None =>
                for {
                  topic <- createTopic()
                  _     <- topicMap.update(_ + (channel -> topic))
                } yield false
              case Some(topic) => async.pure(true)
          _ <- logger.info(
            s"Creating channel: ${channel} existed=${existed}"
          )
          resp <- existed match
              case false =>
                Ok(
                  JsonObject(
                    "channel" -> Json.fromString(channel),
                    "created" -> Json.True,
                    "info" -> Json.fromString(
                      s"Successfully created channel ${channel}"
                    )
                  )
                )
              case true =>
                Ok(
                  JsonObject(
                    "channel" -> Json.fromString(channel),
                    "created" -> Json.False,
                    "info" -> Json.fromString(
                      s"Channel ${channel} already exists"
                    )
                  )
                )
        } yield resp

      case GET -> Root / "status" / channel =>
        for {
          maybeTopic <- getTopic(channel)
          _ <- logger.info(
            s"Querying channel: ${channel} existed=${maybeTopic.nonEmpty}"
          )
          resp <- maybeTopic match
              case None =>
                Ok(
                  JsonObject(
                    "channel" -> Json.fromString(channel),
                    "status"  -> Json.False,
                    "info" -> Json.fromString(
                      s"Channel ${channel} not found. Please create it first"
                    )
                  )
                )
              case Some(_) =>
                Ok(
                  JsonObject(
                    "channel" -> Json.fromString(channel),
                    "status"  -> Json.True,
                    "info" -> Json.fromString(
                      s"Channel ${channel} is alive"
                    )
                  )
                )
        } yield resp

      case GET -> Root / "delete" / channel =>
        for {
          maybeTopic <- getTopic(channel)
          _ <- logger.info(
            s"Deleting channel: ${channel} existed=${maybeTopic.nonEmpty}"
          )
          resp <- maybeTopic match
              case None =>
                NotFound(
                  JsonObject(
                    "channel" -> Json.fromString(channel),
                    "deleted" -> Json.False,
                    "info" -> Json.fromString(
                      s"Channel ${channel} not found. Please create it first"
                    )
                  )
                )
              case Some(queue) =>
                topicMap.update(_ - channel) *> Ok(
                  JsonObject(
                    "channel" -> Json.fromString(channel),
                    "deleted" -> Json.True,
                    "info" -> Json.fromString(
                      s"Successfully deleted channel ${channel}"
                    )
                  )
                )
        } yield resp

      case req @ POST -> Root / "send" / channel =>
        for {
          in         <- req.as[InEvent]
          maybeTopic <- getTopic(channel)
          resp <- maybeTopic match
              case None =>
                NotFound(
                  JsonObject(
                    "channel" -> Json.fromString(channel),
                    "send"    -> Json.False,
                    "info" -> Json.fromString(
                      s"Channel ${channel} not found. Please create it first"
                    )
                  )
                )
              case Some(topic) => {
                val out = in.toOutEvent
                topic
                  .publish1(Some(out))
                  .flatMap(pubed =>
                    if pubed.isRight then
                        Ok(
                          JsonObject(
                            "channel" -> Json.fromString(channel),
                            "send"    -> Json.True,
                            "info" -> Json.fromString(
                              s"Successfully send a MESSAGE to ${channel}"
                            )
                          )
                        )
                    else
                        NotFound(
                          JsonObject(
                            "channel" -> Json.fromString(channel),
                            "send"    -> Json.False,
                            "info" -> Json.fromString(
                              s"Failed to send a MESSAGE to ${channel}"
                            )
                          )
                        )
                  )
              }
        } yield resp

      case GET -> Root / "ws" / channel =>
        getTopic(channel)
          .flatMap {
            _ match
                case None =>
                  NotFound(
                    JsonObject(
                      "channel" -> Json.fromString(channel),
                      "ws"      -> Json.False,
                      "info" -> Json.fromString(
                        s"Channel ${channel} not found. Please create it first"
                      )
                    )
                  )
                case Some(topic) => {
                  val fromClient: Pipe[F, WebSocketFrame, Unit] = _.evalMap {
                    case Text(s, _) =>
                      logger.debug(s"[channel=$channel] WS Received: $s")
                    case _ => async.unit
                  }
                  val toClient =
                    topic.subscribeUnbounded.map {
                      case Some(msg) =>
                        msg match
                            case e: OutEvent => Text(e.asJson.noSpaces)
                            case _           => Close()
                      case None => Close()
                    }
                  wsb.build(toClient, fromClient)
                }
          }
    }
}

object WSRoute {
  def apply[F[_]: Async: LoggerFactory](
    topicMapRef: TopicMapRef[F]
  ): WSRoute[F] =
    new WSRoute[F](topicMapRef)
}
