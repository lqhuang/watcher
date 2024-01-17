/*
 * Copyright 2023 Lanqing Huang
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package io.lqhuang
package watcher
package types

import cats.effect.std.AtomicCell
import fs2.concurrent.Topic

import data.*

type Message           = Any
type WTopic[F[_]]      = Topic[F, Option[Message]]
type TopicMapRef[F[_]] = AtomicCell[F, Map[String, WTopic[F]]]
