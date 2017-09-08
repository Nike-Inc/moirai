package com.nike.moirai.resource.reload

import nl.jqno.equalsverifier.EqualsVerifier
import org.scalatest.{FunSpec, Matchers}

class ResourceReloadSettingsSpec extends FunSpec with Matchers {
  describe("ResourceReloadSettings equals and hashCode") {
    it("should satisfy their contract") {
      EqualsVerifier.forClass(classOf[ResourceReloadSettings]).usingGetClass().verify()
    }
  }
}
