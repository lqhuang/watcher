/*
 * Copyright 2023 Lanqing Huang
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package io.lqhuang
package watcher
package types

import cats.effect.kernel.Ref
import cats.effect.std.Queue

import data.*

type EventQueue[F[_]]  = Queue[F, Option[OutEvent]]
type QueueMapRef[F[_]] = Ref[F, Map[String, EventQueue[F]]]
