package com.nike.moirai

import java.util.concurrent.CompletableFuture

import org.scalatest.{FunSpec, Matchers}

class SuppliersSpec  extends FunSpec with Matchers {
  describe("supplierAndThen") {
    it("should apply the function to the result") {
      Suppliers.supplierAndThen[String, String](() => "abc", _.toUpperCase).get() shouldBe "ABC"
    }
  }

  describe("futureSupplierAndThen") {
    it("should apply the function to the result") {
      Suppliers.futureSupplierAndThen[String, Int](
        () => CompletableFuture.completedFuture("abc"),
        _.length
      ).get().get() shouldBe 3
    }

    it("should do return a failed future if the future fails") {
      Suppliers.futureSupplierAndThen[String, Int](
        () => CompletableFuture.supplyAsync(() => throw new RuntimeException("error")),
        _.length
      ).get().exceptionally(_ => 7).get() shouldBe 7
    }
  }

  describe("async") {
    it("should wrap a supplier into a CompletableFuture") {
      Suppliers.async[String](() => "abc").get().get() shouldBe "abc"
    }
  }
}
