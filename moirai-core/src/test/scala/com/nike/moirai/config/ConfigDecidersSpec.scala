package com.nike.moirai.config

import java.time.Instant

import com.nike.moirai.FeatureCheckInput
import org.scalatest.{FunSpec, Matchers}

class ConfigDecidersSpec extends FunSpec with Matchers {
  describe("userIdCheck") {
    describe("for FeatureCheckInput with a userId") {
      val featureCheckInput = FeatureCheckInput.forUser("8675309")

      it("should return true for a predicate that returns true for that user") {
        ConfigDeciders.userIdCheck(featureCheckInput, (userId) => userId == "8675309") shouldBe true
      }

      it("should return false for a predicate that returns true for that user") {
        ConfigDeciders.userIdCheck(featureCheckInput, (userId) => userId != "8675309") shouldBe false
      }
    }

    describe("for FeatureCheckInput without a userId") {
      val featureCheckInput = new FeatureCheckInput.Builder().dateTime(Instant.now()).build()

      it("should return false for a predicate that returns true for any user") {
        ConfigDeciders.userIdCheck(featureCheckInput, (_) => true) shouldBe false
      }

      it("should return false for a predicate that returns false for any user") {
        ConfigDeciders.userIdCheck(featureCheckInput, (_) => false) shouldBe false
      }
    }
  }
}
