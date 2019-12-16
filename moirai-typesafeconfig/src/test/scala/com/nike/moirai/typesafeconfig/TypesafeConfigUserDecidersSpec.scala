package com.nike.moirai.typesafeconfig

import com.nike.moirai.resource.FileResourceLoaders
import com.nike.moirai.{ConfigFeatureFlagChecker, FeatureCheckInput, Suppliers}
import com.typesafe.config.Config
import org.scalatest.{FunSpec, Matchers}

//noinspection TypeAnnotation
class TypesafeConfigUserDecidersSpec extends FunSpec with Matchers {

  describe("A combined enabled-user and proportion-of-users config decider") {
    val resourceLoader = FileResourceLoaders.forClasspathResource("moirai.conf")

    val featureFlagChecker = ConfigFeatureFlagChecker.forConfigSupplier[Config](
      Suppliers.supplierAndThen(resourceLoader, TypesafeConfigReader.FROM_STRING),
      TypesafeConfigDecider.ENABLED_USERS.or(TypesafeConfigDecider.PROPORTION_OF_USERS)
    )

    describe("a feature specifying both enabledUserIds and an enabledProportion of 0.0") {
      it("should be enabled for enabled users") {
        featureFlagChecker.isFeatureEnabled("foo", FeatureCheckInput.forUser("7")) shouldBe true
        featureFlagChecker.isFeatureEnabled("foo", FeatureCheckInput.forUser("8")) shouldBe true
        featureFlagChecker.isFeatureEnabled("foo", FeatureCheckInput.forUser("9")) shouldBe true
      }

      it("should be enabled for enabled users from common-value reference") {
        featureFlagChecker.isFeatureEnabled("foo", FeatureCheckInput.forUser("42")) shouldBe true
      }

      it("it should not be enabled for non-whitelist users because proportion is 0.0") {
        featureFlagChecker.isFeatureEnabled("foo", FeatureCheckInput.forUser("6")) shouldBe false
      }
    }

    describe("a feature specifying only enabledProportion of 1.0") {
      it("should be enabled for all users because proportion is 1.0") {
        featureFlagChecker.isFeatureEnabled("bar", FeatureCheckInput.forUser("7")) shouldBe true
        featureFlagChecker.isFeatureEnabled("bar", FeatureCheckInput.forUser("6")) shouldBe true
        featureFlagChecker.isFeatureEnabled("bar", FeatureCheckInput.forUser("Zaphod Beeblebrox")) shouldBe true
      }
    }

    describe("a feature specifying only enabledUserIds") {
      it("should be enabled for enabled users") {
        featureFlagChecker.isFeatureEnabled("baz", FeatureCheckInput.forUser("susan")) shouldBe true
        featureFlagChecker.isFeatureEnabled("baz", FeatureCheckInput.forUser("bill")) shouldBe true
      }


      it("should be enabled for enabled users from common-value reference") {
        featureFlagChecker.isFeatureEnabled("foo", FeatureCheckInput.forUser("42")) shouldBe true
      }

      it("should not be enabled for non-enabled users") {
        featureFlagChecker.isFeatureEnabled("baz", FeatureCheckInput.forUser("jack")) shouldBe false
      }
    }

    describe("a feature present in the config with no configuration entries") {
      it("should not be enabled for any users") {
        featureFlagChecker.isFeatureEnabled("qux", FeatureCheckInput.forUser("42")) shouldBe false
      }
    }

    describe("a feature not specified in the config") {
      it("should not be enabled for any users") {
        featureFlagChecker.isFeatureEnabled("quux", FeatureCheckInput.forUser("42")) shouldBe false
      }
    }
  }

  describe("A featureEnabled config decider") {
    val resourceLoader = FileResourceLoaders.forClasspathResource("moirai.conf")

    val featureFlagChecker = ConfigFeatureFlagChecker.forConfigSupplier[Config](
      Suppliers.supplierAndThen(resourceLoader, TypesafeConfigReader.FROM_STRING),
      TypesafeConfigDecider.FEATURE_ENABLED
    )

    it("should be enabled if the configuration say so") {
      featureFlagChecker.isFeatureEnabled("coffee") shouldBe true
      featureFlagChecker.isFeatureEnabled("coffee", FeatureCheckInput.forUser("42")) shouldBe true
    }

    it("should be disabled if the configuration say so") {
      featureFlagChecker.isFeatureEnabled("tea") shouldBe false
      featureFlagChecker.isFeatureEnabled("tea", FeatureCheckInput.forUser("42")) shouldBe false
    }

    it("should be disabled if the configuration does not specify anything") {
      featureFlagChecker.isFeatureEnabled("water") shouldBe false
      featureFlagChecker.isFeatureEnabled("water", FeatureCheckInput.forUser("42")) shouldBe false
    }
  }

  describe("A custom dimension enabled valued config decider") {
    val resourceLoader = FileResourceLoaders.forClasspathResource("moirai.conf")

    val featureFlagChecker = ConfigFeatureFlagChecker.forConfigSupplier[Config](
      Suppliers.supplierAndThen(resourceLoader, TypesafeConfigReader.FROM_STRING),
      TypesafeConfigDecider.enabledCustomStringDimension("country", "enabledCountries")
    )

    it("should be enabled for a user in an enabled country") {
      val input = FeatureCheckInput.forUser("bob").withAdditionalDimension("country", "Peru")
      featureFlagChecker.isFeatureEnabled("qux", input) shouldBe true
    }

    it("should be disabled for a user not in an enabled country") {
      val input = FeatureCheckInput.forUser("bob").withAdditionalDimension("country", "Belgium")
      featureFlagChecker.isFeatureEnabled("qux", input) shouldBe false
    }

    it("should be disabled for a user not in a country") {
      val input = FeatureCheckInput.forUser("bob")
      featureFlagChecker.isFeatureEnabled("qux", input) shouldBe false
    }

    it("should be disabled for a user with an invalid value for country") {
      val input = FeatureCheckInput.forUser("bob").withAdditionalDimension("country", 8)
      featureFlagChecker.isFeatureEnabled("qux", input) shouldBe false
    }
  }
}
