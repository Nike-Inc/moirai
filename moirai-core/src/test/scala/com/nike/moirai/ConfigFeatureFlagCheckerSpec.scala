package com.nike.moirai

import java.time.Duration
import java.time.temporal.ChronoUnit.MILLIS

import com.nike.moirai.resource.reload.{ResourceReloadSettings, ResourceReloader}
import org.scalatest.{FunSpec, Matchers}

class ConfigFeatureFlagCheckerSpec extends FunSpec with Matchers {
  describe("checker for config supplier") {
    val config = Map(
      "feature1" -> true
    )

    val configFeatureFlagChecker = ConfigFeatureFlagChecker.forConfigSupplier[Map[String, Boolean]](
      () => config,
      (configDecisionInput) => configDecisionInput.getConfig.getOrElse(configDecisionInput.getFeatureIdentifier, false)
    )

    it("should return true for a feature that maps to true in the config") {
      configFeatureFlagChecker.isFeatureEnabled("feature1", FeatureCheckInput.forUser("a")) shouldBe true
    }

    it("should return false for another feature that is not in the config") {
      configFeatureFlagChecker.isFeatureEnabled("feature2", FeatureCheckInput.forUser("a")) shouldBe false
    }
  }

  describe("checker for resource reloader") {
    val initialConfig = Map(
      "feature1" -> true
    )

    val updatedConfig = Map(
      "feature1" -> false,
      "feature2" -> true
    )

    val configFeatureFlagChecker = ConfigFeatureFlagChecker.forReloadableResource[Map[String, Boolean]](
      ResourceReloader.withCustomSettings(
        Suppliers.async(() => updatedConfig),
        initialConfig,
        new ResourceReloadSettings(Duration.of(100L, MILLIS), Duration.of(10L, MILLIS))
      ),
      (configDecisionInput) => configDecisionInput.getConfig.getOrElse(configDecisionInput.getFeatureIdentifier, false)
    )

    it("should initially return true for a feature that maps to true in the initial config") {
      configFeatureFlagChecker.isFeatureEnabled("feature1", FeatureCheckInput.forUser("a")) shouldBe true
    }

    it("should initially return false for a feature that is not in the initial config") {
      configFeatureFlagChecker.isFeatureEnabled("feature2", FeatureCheckInput.forUser("a")) shouldBe false
    }

    it("should return false for a feature that is changed to false in the updated config after a reload cycle") {
      Thread.sleep(250)
      configFeatureFlagChecker.isFeatureEnabled("feature1", FeatureCheckInput.forUser("a")) shouldBe false
    }

    it("should return true for a feature that maps to true in the updated config") {
      configFeatureFlagChecker.isFeatureEnabled("feature2", FeatureCheckInput.forUser("a")) shouldBe true
    }
  }
}
