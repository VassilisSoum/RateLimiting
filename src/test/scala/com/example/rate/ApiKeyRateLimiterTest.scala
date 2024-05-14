package com.example.rate

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.implicits._
import org.scalatest.Assertion
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration._

class ApiKeyRateLimiterTest extends AsyncFreeSpec with AsyncIOSpec with Matchers {
  val burstCapacity: Int = 5
  val refillRate: Int = 5
  val refillInterval: FiniteDuration = 500.millis
  val rateLimiter = new ApiKeyRateLimiter(burstCapacity, refillRate, refillInterval)

  "ApiKeyRateLimiter should" - {
    "handle requests independently for each API key ensuring token bucket uniqueness" in {
      testApiKeyIndependence("key1", "key2")
    }

    "allow additional requests after refill interval" in {
      testRefillInterval("key3")
    }
  }

  def testApiKeyIndependence(apiKey1: String, apiKey2: String): IO[Assertion] =
    for {
      _ <- rateLimiter.initializeLimiter(apiKey1)
      _ <- rateLimiter.initializeLimiter(apiKey2)
      results1 <- List.fill(burstCapacity)(apiKey1).traverse(rateLimiter.tryTake)
      results2 <- List.fill(burstCapacity)(apiKey2).traverse(rateLimiter.tryTake)
      extraTry1 <- rateLimiter.tryTake(apiKey1)
      extraTry2 <- rateLimiter.tryTake(apiKey2)
    } yield {
      assert(results1.forall(_ == true))
      assert(results2.forall(_ == true))
      extraTry1 shouldBe false
      extraTry2 shouldBe false
    }

  def testRefillInterval(apiKey: String): IO[Assertion] =
    for {
      _ <- rateLimiter.initializeLimiter(apiKey)
      _ <- List.fill(burstCapacity)(apiKey).traverse(rateLimiter.tryTake)
      _ <- IO.sleep(refillInterval * 2)
      result <- rateLimiter.tryTake(apiKey)
    } yield result shouldBe true
}
