package com.nike.moirai.config

import nl.jqno.equalsverifier.EqualsVerifier
import org.scalatest.{FunSpec, Matchers}

class ConfigDecisionInputSpec extends FunSpec with Matchers {
  describe("ConfigDecisionInput equals and hashCode") {
    it("should satisfy their contract") {
      EqualsVerifier.forClass(classOf[ConfigDecisionInput[Object]]).usingGetClass().verify()
    }
  }
}
