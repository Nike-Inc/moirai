package com.nike.moirai.config

import java.util

import com.nike.moirai.FeatureCheckInput
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{FunSpec, Matchers}

import scala.collection.JavaConverters._

class EnabledCustomDimensionConfigDeciderSpec extends FunSpec with Matchers with GeneratorDrivenPropertyChecks {
  val dimKey = "numberOfThings"

  describe("A basic implementation of WhitelistedUsersConfigDecider") {
    val decider = new EnabledCustomDimensionConfigDecider[Map[String, Seq[String]], Int]() {
      override protected def dimensionKey(): String = dimKey
      override protected def enabledValues(config: Map[String, Seq[String]], featureIdentifier: String): util.Collection[Int] =
        config.get(featureIdentifier).map(_.map(_.toInt).asJava).getOrElse(java.util.Collections.emptyList())
    }

    val config = Map(
      "feature1" -> Seq("7", "23"),
      "feature2" -> Seq("16")
    )

    describe("a feature that returns [7, 23]") {
      val feature = "feature1"

      it("should return true for user with dimension value 7") {
        decider.test(
          new ConfigDecisionInput(config, feature, FeatureCheckInput.forUser("a").withAdditionalDimension(dimKey, 7))) shouldBe true
      }

      it("should return true for user with dimension value 23") {
        decider.test(
          new ConfigDecisionInput(config, feature, FeatureCheckInput.forUser("a").withAdditionalDimension(dimKey, 23))) shouldBe true
      }

      it("should return false for any other user") {
        forAll { numberOfThings: Int =>
          whenever(numberOfThings != 7 && numberOfThings != 23) {
            decider.test(
              new ConfigDecisionInput(config, feature, FeatureCheckInput.forUser("a").withAdditionalDimension(dimKey, numberOfThings))) shouldBe false
          }
        }
      }
    }

    describe("a feature that returns [16]") {
      val feature = "feature2"

      it("should return true for user with dimension value 16") {
        decider.test(
          new ConfigDecisionInput(config, feature, FeatureCheckInput.forUser("a").withAdditionalDimension(dimKey, 16))) shouldBe true
      }

      it("should return false for any other user") {
        forAll { numberOfThings: Int =>
          whenever(numberOfThings != 16) {
            decider.test(
              new ConfigDecisionInput(config, feature, FeatureCheckInput.forUser("a").withAdditionalDimension(dimKey, numberOfThings))) shouldBe false
          }
        }
      }
    }

    describe("a feature without a configured whitelist") {
      val feature = "feature3"

      it("should return false for any user") {
        forAll { numberOfThings: String =>
          decider.test(
            new ConfigDecisionInput(config, feature, FeatureCheckInput.forUser("a").withAdditionalDimension(dimKey, numberOfThings))) shouldBe false
        }
      }
    }
  }
}
