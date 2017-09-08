package com.nike.moirai.resource.reload

import java.time.Duration
import java.time.temporal.ChronoUnit.MILLIS

import com.nike.moirai.Suppliers
import org.scalatest.{FunSpec, Matchers}

class ResourceReloaderSpec extends FunSpec with Matchers {
  describe("ResourceReloader for resource that returns an incrementing value, and sometimes throws an error or takes too long") {
    class MockSupplier() {
      var v = 0

      def value(): Int = {
        v += 1

        if (v == 3) throw new RuntimeException("")
        if (v == 4) Thread.sleep(25)

        v
      }
    }

    val mock = new MockSupplier

    val resourceReloader = ResourceReloader.withCustomSettingsAndManualLifecycle(
      Suppliers.async(() => mock.value()),
      -178,
      new ResourceReloadSettings(Duration.of(100L, MILLIS), Duration.of(10L, MILLIS))
    )

    it("should return the initial value before reloading starts") {
      resourceReloader.getValue shouldBe -178
    }

    it("should reload the value and return 1 after the reload once reloading is initialized") {
      resourceReloader.init()
      Thread.sleep(150)
      resourceReloader.getValue shouldBe 1
    }

    it("should reload the value and at least 2 after the another reload cycle") {
      Thread.sleep(150)
      resourceReloader.getValue should be >= 2
    }

    it("should reload the value and still be at least 2 after the another reload cycle where an exception would have occurred") {
      Thread.sleep(150)
      resourceReloader.getValue should be >= 2
    }

    it("should reload the value and still be at least 2 after the another reload cycle where a timeout would have occurred") {
      Thread.sleep(150)
      resourceReloader.getValue should be >= 2
    }

    it("should reload the value and be at least 5 after the reload cycles") {
      Thread.sleep(150)
      resourceReloader.getValue should be >= 5
    }

    it("should reload the value and still return the last value after the reloader is shutdown") {
      resourceReloader.shutdown()
      Thread.sleep(150)
      val lastValue = resourceReloader.getValue
      lastValue should be >= 5
      Thread.sleep(150)
      resourceReloader.getValue shouldBe lastValue
    }
  }
}
