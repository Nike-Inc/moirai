package com.nike.moirai.config

import java.util

import com.nike.moirai.FeatureCheckInput
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{FunSpec, Matchers}

import scala.collection.JavaConverters._

class EnabledUsersConfigDeciderSpec extends FunSpec with Matchers with GeneratorDrivenPropertyChecks {
  describe("A basic implementation of WhitelistedUsersConfigDecider") {
    val decider = new EnabledUsersConfigDecider[Map[String, Seq[String]]]() {
      override protected def enabledUsers(config: Map[String, Seq[String]], featureIdentifier: String): util.Collection[String] =
        config.get(featureIdentifier).map(_.asJava).getOrElse(java.util.Collections.emptyList())
    }

    val config = Map(
      "feature1" -> Seq("a", "b"),
      "feature2" -> Seq("c")
    )

    describe("a feature that returns [a, b]") {
      val feature = "feature1"

      it("should return true for user a") {
        decider.test(new ConfigDecisionInput(config, feature, FeatureCheckInput.forUser("a"))) shouldBe true
      }

      it("should return true for user b") {
        decider.test(new ConfigDecisionInput(config, feature, FeatureCheckInput.forUser("b"))) shouldBe true
      }

      it("should return false for any other user") {
        forAll { userId: String =>
          whenever(userId != "a" && userId != "b") {
            decider.test(new ConfigDecisionInput(config, feature, FeatureCheckInput.forUser(userId))) shouldBe false
          }
        }
      }
    }

    describe("a feature that returns [c]") {
      val feature = "feature2"

      it("should return true for user c") {
        decider.test(new ConfigDecisionInput(config, feature, FeatureCheckInput.forUser("c"))) shouldBe true
      }

      it("should return false for any other user") {
        forAll { userId: String =>
          whenever(userId != "c") {
            decider.test(new ConfigDecisionInput(config, feature, FeatureCheckInput.forUser(userId))) shouldBe false
          }
        }
      }
    }

    describe("a feature without a configured whitelist") {
      val feature = "feature3"

      it("should return false for any user") {
        forAll { userId: String =>
          decider.test(new ConfigDecisionInput(config, feature, FeatureCheckInput.forUser(userId))) shouldBe false
        }
      }
    }
  }
}
