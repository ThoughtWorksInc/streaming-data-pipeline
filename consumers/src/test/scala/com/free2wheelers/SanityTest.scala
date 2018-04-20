package com.free2wheelers

import org.scalatest.{FunSpec, Matchers}

class SanityTest extends FunSpec with Matchers {

  describe("Should have sanity") {
    it("should be true when true") {
      1 should be (1)
    }
  }

}
