package com.example.rate

import cats.effect.{IO, Ref}

import scala.concurrent.duration.FiniteDuration

class TokenBucketRateLimiter(
    burstCapacity: Int,
    refillRate: Int,
    refillInterval: FiniteDuration
) {
  private val availableTokens = Ref.unsafe[IO, Int](burstCapacity)

  def tryTake: IO[Boolean] =
    availableTokens.modify {
      case currentTokens if currentTokens > 0 => (currentTokens - 1, true)
      case currentTokens                      => (currentTokens, false) // No tokens available
    }

  def start: IO[Unit] = backgroundRefill.start.void

  private def refill: IO[Unit] =
    IO.sleep(refillInterval) >> availableTokens.update { currentTokens =>
      math.min(burstCapacity, currentTokens + refillRate) // Refill tokens up to the burst capacity
    }

  private def backgroundRefill: IO[Unit] =
    refill.foreverM // Continuously run the refill process
}

class ApiKeyRateLimiter(
    burstCapacity: Int,
    refillRate: Int,
    refillInterval: FiniteDuration
) {
  private type Limiter = TokenBucketRateLimiter
  private val limiters = Ref.unsafe[IO, Map[String, Limiter]](Map.empty)

  def tryTake(apiKey: String): IO[Boolean] =
    for {
      limiter <- getOrCreateLimiter(apiKey)
      result <- limiter.tryTake
    } yield result

  def initializeLimiter(apiKey: String): IO[Unit] =
    getOrCreateLimiter(apiKey).flatMap(_.start) // Start the limiter's background refill process

  private def getOrCreateLimiter(apiKey: String): IO[Limiter] =
    limiters.modify { currentMap =>
      currentMap.get(apiKey) match {
        case Some(limiter) => (currentMap, limiter)
        case None =>
          val newLimiter = new TokenBucketRateLimiter(burstCapacity, refillRate, refillInterval)
          (currentMap.updated(apiKey, newLimiter), newLimiter)
      }
    }
}
