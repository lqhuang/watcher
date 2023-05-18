package io.lqhuang
package watcher
package types

import cats.effect.kernel.Ref
import cats.effect.std.Queue

import data.*

type QueueMapRef[F[_]] = Ref[F, Map[String, Queue[F, Option[OutEvent]]]]
