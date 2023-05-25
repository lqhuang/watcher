/*
 * Copyright 2023 Lanqing Huang
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package io.lqhuang
package watcher
package types

// import cats.effect.kernel.Ref
import cats.effect.std.{AtomicCell, Queue}

import data.*

type EventQueue[F[_]]  = Queue[F, Option[OutEvent]]
type QueueMapRef[F[_]] = AtomicCell[F, Map[String, EventQueue[F]]]
