package com.nike.moirai.config

import java.lang
import java.util.Optional

import com.nike.moirai.FeatureCheckInput
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{FunSpec, Matchers}

import scala.collection.mutable
import scala.compat.java8.OptionConverters._

class ProportionOfUsersConfigDeciderSpec extends FunSpec with Matchers with GeneratorDrivenPropertyChecks {
  describe("A basic implementation of ProportionOfUsersConfigDecider") {
    val decider = new ProportionOfUsersConfigDecider[Map[String, Double]]() {
      override protected def enabledProportion(config: Map[String, Double], featureIdentifier: String): Optional[lang.Double] =
        config.get(featureIdentifier).map(Double.box).asJava
    }

    val config = Map(
      "feature1" -> 0.0,
      "feature2" -> 1.0,
      "feature3" -> 0.7
    )

    describe("a feature that returns 0.0") {
      val feature = "feature1"

      it("should return false for any user") {
        forAll { (userId: String) =>
          decider.test(new ConfigDecisionInput(config, feature, FeatureCheckInput.forUser(userId))) shouldBe false
        }
      }
    }

    describe("a feature that returns 1.0") {
      val feature = "feature2"

      it("should return true for any user") {
        forAll { (userId: String) =>
          decider.test(new ConfigDecisionInput(config, feature, FeatureCheckInput.forUser(userId))) shouldBe true
        }
      }
    }

    describe("a feature that returns 0.7") {
      val feature = "feature3"

      it("should return true for approximately 70% of users") {
        val results = mutable.Buffer.empty[Boolean]

        implicit val generatorDrivenConfig = PropertyCheckConfiguration(minSuccessful = 10000)

        forAll { (userId: String) =>
          results += decider.test(new ConfigDecisionInput(config, feature, FeatureCheckInput.forUser(userId)))
        }

        results.count(identity) / results.size.toDouble should be (0.7 +- 0.05)
      }
    }

    describe("a feature without a configured proportion") {
      val feature = "feature4"

      it("should return false for any user") {
        forAll { (userId: String) =>
          decider.test(new ConfigDecisionInput(config, feature, FeatureCheckInput.forUser(userId))) shouldBe false
        }
      }
    }
  }
}
