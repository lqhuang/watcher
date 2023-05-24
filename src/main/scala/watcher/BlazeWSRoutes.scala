/*
 * Copyright 2023 Lanqing Huang
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package io.lqhuang
package watcher

import scala.concurrent.duration.*

import cats.syntax.all.*
import cats.effect.Async
import cats.effect.kernel.Ref
import cats.effect.std.Queue
import fs2.{Pipe, Stream}
import fs2.concurrent.Topic
import io.circe.syntax.*

import org.http4s.dsl.Http4sDsl
import org.http4s.implicits.*
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
import types.{EventQueue, QueueMapRef}

class BlazeWS[F[_]: LoggerFactory](using async: Async[F])()
  extends Http4sDsl[F] {

  val logger = LoggerFactory[F].getLogger

  val mapRefContext: F[QueueMapRef[F]] =
    Ref[F].of(Map.empty[String, EventQueue[F]])

  def getQueue(key: String): F[Option[EventQueue[F]]] =
    mapRefContext.flatMap(ref => ref.get.map(x => x.get(key)))

  def routes(wsb: WebSocketBuilder2[F]): HttpRoutes[F] =
    HttpRoutes.of[F] {
      case GET -> Root =>
        Ok("You got me :)")

      case GET -> Root / "list-channels" =>
        for {
          mapRef       <- mapRefContext
          jsonChannels <- mapRef.get.map(_.keys.toSeq.asJson)
          resp         <- Ok(jsonChannels)
        } yield resp

      case GET -> Root / "create" / channel =>
        for {
          maybeQueue <- getQueue(channel)
          mapRef     <- mapRefContext
          existed <- maybeQueue match
            case None =>
              Queue
                .unbounded[F, Option[OutEvent]]
                .map(queue => mapRef.update(_.updated(channel, queue)))
                .map(_ => false)
            case Some(queue) => async.pure(true)
          resp <- existed match
            case false =>
              Ok(
                WatcherResponse(
                  s"Successfully created channel ${channel}"
                )
              )
            case true =>
              Ok(
                WatcherResponse(
                  s"Channel ${channel} already exists"
                )
              )
        } yield resp

      case GET -> Root / "status" / channel =>
        for {
          maybeQueue <- getQueue(channel)
          resp       <- Ok(s"Successfully send a GET request to ${channel}")
        } yield resp

      case GET -> Root / "delete" / channel =>
        for {
          maybeQueue <- getQueue(channel)
          mapRef     <- mapRefContext
          resp <- maybeQueue match
            case None =>
              NotFound(
                WatcherResponse(
                  s"Channel ${channel} not found. Please create it first"
                )
              )
            case Some(queue) =>
              mapRef.update(_ - channel) *> Ok(
                WatcherResponse(
                  s"Successfully deleted channel ${channel}"
                )
              )
        } yield resp

      case req @ POST -> Root / "send" / channel =>
        for {
          in         <- req.as[InEvent]
          maybeQueue <- getQueue(channel)
          resp <- maybeQueue match
            case None =>
              NotFound(
                WatcherResponse(
                  s"Channel ${channel} not found. Please create it first"
                )
              )
            case Some(queue) => {
              val out = in.toOutEvent
              queue.offer(Some(out)) *> Ok(
                WatcherResponse(
                  s"Successfully send a GET request to ${channel}"
                )
              )
            }
        } yield resp

      // // alive route
      // case GET -> Root / "ws" => {
      //   val toClient: Stream[F, WebSocketFrame] =
      //     Stream
      //       .awakeEvery[F](2.seconds)
      //       .map(d => Text(s"Hello! ${d.toSeconds}"))
      //   val fromClient: Pipe[F, WebSocketFrame, Unit] = _.evalMap {
      //     case Text(s, _) => logger.info(s)
      //     case Close(_)   => logger.info("Got CLOSE")
      //     case f          => logger.info(s"Unknown type: $f")
      //   }
      //   wsb.build(toClient, fromClient)
      // }

      case GET -> Root / "ws" / channel =>
        getQueue(channel)
          .flatMap {
            _ match
              case None =>
                NotFound(
                  WatcherResponse(
                    s"Channel ${channel} not found. Please create it first"
                  )
                )
              case Some(queue) => {
                val fromClient: Pipe[F, WebSocketFrame, Unit] = _.evalMap {
                  case Text(s, _) =>
                    logger.info(s"[channel=$channel] WS Received: $s")
                  case _ => async.unit
                }
                val toClient =
                  Stream
                    .fromQueueUnterminated(queue)
                    .map(_.asJson.noSpaces)
                    .map(Text(_))
                wsb.build(toClient, fromClient)
              }
          }
    }
}

object BlazeWS {
  def apply[F[_]: Async: LoggerFactory](): BlazeWS[F] =
    new BlazeWS[F]()
}
