package com.nike.moirai.typesafeconfig

import com.nike.moirai.resource.FileResourceLoaders
import com.nike.moirai.{ConfigFeatureFlagChecker, FeatureCheckInput, Suppliers}
import com.typesafe.config.Config
import org.scalatest.{FunSpec, Matchers}

//noinspection TypeAnnotation
class TypesafeConfigUserDecidersSpec extends FunSpec with Matchers {
  val resourceLoader = FileResourceLoaders.forClasspathResource("moirai.conf")

  val featureFlagChecker = ConfigFeatureFlagChecker.forConfigSupplier[Config](
    Suppliers.supplierAndThen(resourceLoader, TypesafeConfigReader.FROM_STRING),
    TypesafeConfigDecider.WHITELISTED_USERS.or(TypesafeConfigDecider.PROPORTION_OF_USERS)
  )

  describe("A combined whitelisted-user and proportion-of-users config decider") {
    describe("a feature specifying both whitelistedUserIds and an enabledProportion of 0.0") {
      it("should be enabled for whitelisted users") {
        featureFlagChecker.isFeatureEnabled("foo", FeatureCheckInput.forUser("7")) shouldBe true
        featureFlagChecker.isFeatureEnabled("foo", FeatureCheckInput.forUser("8")) shouldBe true
        featureFlagChecker.isFeatureEnabled("foo", FeatureCheckInput.forUser("9")) shouldBe true
      }

      it("should be enabled for whitelisted users from common-value reference") {
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

    describe("a feature specifying only whitelistedUserIds") {
      it("should be enabled for whitelisted users") {
        featureFlagChecker.isFeatureEnabled("baz", FeatureCheckInput.forUser("susan")) shouldBe true
        featureFlagChecker.isFeatureEnabled("baz", FeatureCheckInput.forUser("bill")) shouldBe true
      }


      it("should be enabled for whitelisted users from common-value reference") {
        featureFlagChecker.isFeatureEnabled("foo", FeatureCheckInput.forUser("42")) shouldBe true
      }

      it("should not be enabled for non-whitelisted users") {
        featureFlagChecker.isFeatureEnabled("baz", FeatureCheckInput.forUser("jack")) shouldBe false
      }
    }
  }
}
