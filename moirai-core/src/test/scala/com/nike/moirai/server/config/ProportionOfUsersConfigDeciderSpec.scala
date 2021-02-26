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

      override protected def featureGroup(config: Map[String, Double], featureIdentifier: String): Optional[String] = Optional.empty()
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

        implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 10000)

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

    describe("featureGroup") {
      val decider = new ProportionOfUsersConfigDecider[Map[String, Double]]() {
        override protected def enabledProportion(config: Map[String, Double], featureIdentifier: String): Optional[lang.Double] =
          config.get(featureIdentifier).map(Double.box).asJava

        override protected def featureGroup(config: Map[String, Double], featureIdentifier: String): Optional[String] = featureIdentifier match {
          case "feature1" => Optional.of("group1")
          case "feature2" => Optional.of("group1")
          case "feature3" => Optional.of("group2")
          case "feature4" => Optional.of("group2")
          case _ => Optional.empty()
        }
      }

      val config = Map(
        "feature1" -> 0.5,
        "feature2" -> 0.5,
        "feature3" -> 0.5,
        "feature4" -> 0.5,
        "feature5" -> 0.5
      )

      it("should decide features are enabled for the same users if the features are in the same feature group and have the same proportion enabled") {
        val feature1EnabledUsers = mutable.Buffer.empty[String]
        val feature2EnabledUsers = mutable.Buffer.empty[String]
        val feature3EnabledUsers = mutable.Buffer.empty[String]
        val feature4EnabledUsers = mutable.Buffer.empty[String]
        val feature5EnabledUsers = mutable.Buffer.empty[String]

        implicit val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 1000)

        forAll { (userId: String) =>
          List(
            ("feature1", feature1EnabledUsers),
            ("feature2", feature2EnabledUsers),
            ("feature3", feature3EnabledUsers),
            ("feature4", feature4EnabledUsers),
            ("feature5", feature5EnabledUsers)).foreach {
            case (feature, buffer) if decider.test(new ConfigDecisionInput(config, feature, FeatureCheckInput.forUser(userId))) => buffer += userId
            case _ =>
          }
        }

        feature1EnabledUsers should contain theSameElementsAs feature2EnabledUsers
        feature3EnabledUsers should contain theSameElementsAs feature4EnabledUsers

        feature1EnabledUsers should not (contain theSameElementsAs feature3EnabledUsers)
        feature1EnabledUsers should not (contain theSameElementsAs feature5EnabledUsers)

        feature3EnabledUsers should not (contain theSameElementsAs feature5EnabledUsers)
      }
    }
  }
}
