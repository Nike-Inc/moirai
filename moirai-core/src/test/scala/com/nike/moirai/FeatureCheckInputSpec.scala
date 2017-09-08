package com.nike.moirai

import nl.jqno.equalsverifier.EqualsVerifier
import org.scalatest.{FunSpec, Matchers}

import scala.collection.JavaConverters._

class FeatureCheckInputSpec  extends FunSpec with Matchers {
  describe("ConfigDecisionInput equals and hashCode") {
    it("should satisfy their contract") {
      EqualsVerifier.forClass(classOf[FeatureCheckInput]).usingGetClass().verify()
    }
  }

  describe("forUser") {
    val now = java.time.Instant.now()
    val featureCheckInput = FeatureCheckInput.forUser("8675309")

    it("should provide the user") {
      featureCheckInput.getUserId.get() shouldBe "8675309"
    }

    it("should provide the current time") {
      featureCheckInput.getDateTime.get().toEpochMilli should be >= now.toEpochMilli
    }
  }

  describe("forUserAtTime") {
    val time = java.time.Instant.ofEpochMilli(1490917936950L)
    val featureCheckInput = FeatureCheckInput.forUserAtTime("123456", time)

    it("should provide the user") {
      featureCheckInput.getUserId.get() shouldBe "123456"
    }

    it("should provide the specified time") {
      featureCheckInput.getDateTime.get().toEpochMilli shouldBe 1490917936950L
    }
  }

  describe("withDimensions") {
    describe("with additional 'foo' dimension") {
      val now = java.time.Instant.now()
      val featureCheckInput = FeatureCheckInput.forUser("8675309").withAdditionalDimensions(Map("foo" -> "bar").asJava)

      it("should provide the user") {
        featureCheckInput.getUserId.get() shouldBe "8675309"
      }

      it("should provide the current time") {
        featureCheckInput.getDateTime.get().toEpochMilli should be >= now.toEpochMilli
      }

      it("should provide the value for 'foo'") {
        featureCheckInput.getDimension("foo").get() shouldBe "bar"
      }

      it("should return empty for a custom dimension not specified") {
        featureCheckInput.getDimension("baz").isPresent shouldBe false
      }
    }

    describe("with conflicting dimension") {
      it("should error on attempt to add custom dimension that already exists") {
        an [IllegalArgumentException] should be thrownBy FeatureCheckInput.forUser("foo").withAdditionalDimensions(Map("USER_ID" -> "bar").asJava)
      }
    }
  }
}
