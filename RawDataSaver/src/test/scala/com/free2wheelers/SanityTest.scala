package com.free2wheelers

import org.scalatest.{FunSpec, Matchers}

class SanityTest extends FunSpec with Matchers {

  describe("Should have sanity") {
    it("should be true when true") {
      1 should be (1)
    }
    it("should not be true when false") {
      1 should not be (0)
    }
  }

}
