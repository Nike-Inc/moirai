package com.nike.moirai.resource

import java.io.File

import org.scalatest.{FunSpec, Matchers}

class FileResourceLoadersSpec extends FunSpec with Matchers {
  describe("forClasspathResource") {
    it("should load a classpath resource as UTF-8") {
      FileResourceLoaders.forClasspathResource("com/nike/moirai/resource/resource.txt").get() shouldBe "Hi! \uD83D\uDE0E"
    }
  }

  describe("forFile") {
    it("should load a file as UTF-8") {
      FileResourceLoaders.forFile(new File("moirai-core/src/test/resources/com/nike/moirai/resource/resource.txt")).get() shouldBe "Hi! \uD83D\uDE0E"
    }
  }
}
