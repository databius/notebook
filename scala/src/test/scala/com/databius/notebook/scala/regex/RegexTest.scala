package com.databius.notebook.scala.regex

import com.databius.notebook.regex.Regex._
import org.scalatest.funspec.AnyFunSpec

class RegexTest extends AnyFunSpec {
  describe("Regex") {
    it("animals/domestic/*") {
      assert(matchWildcard("animals/domestic/*", "animals/domestic/cats"))
      assert(matchWildcard("animals/domestic/*", "animals/domestic/dogs"))
      assert(
        !matchWildcard("animals/domestic/*", "animals/domestic/dogs/beagles"))
    }
    it("animals/*/cats/*") {
      assert(matchWildcard("animals/*/cats/*", "animals/domestic/cats/persian"))
      assert(matchWildcard("animals/*/cats/*", "animals/wild/cats/leopard"))
      assert(
        !matchWildcard("animals/*/cats/*",
          "animals/domestic/cats/persian/grey"))
      assert(
        !matchWildcard("animals/*/cats/*", "animals/domestic/dogs/beagles"))
    }
    it("animals/domestic/dog*") {
      assert(matchWildcard("animals/domestic/dog*", "animals/domestic/dog"))
      assert(matchWildcard("animals/domestic/dog*", "animals/domestic/doggy"))
      assert(
        !matchWildcard("animals/domestic/dog*", "animals/domestic/dog/beagle"))
      assert(!matchWildcard("animals/domestic/dog*", "animals/domestic/cat"))
    }
    it("animals/domestic/>") {
      assert(matchWildcard("animals/domestic/>", "animals/domestic/cats"))
      assert(
        matchWildcard("animals/domestic/>", "animals/domestic/dogs/beagles"))
      assert(!matchWildcard("animals/domestic/>", "animals"))
      assert(!matchWildcard("animals/domestic/>", "animals/domestic"))
      assert(!matchWildcard("animals/domestic/>", "animals/Domestic"))
    }

    it("animals/*/cats/>") {
      assert(
        matchWildcard("animals/*/cats/>", "animals/domestic/cats/tabby/grey"))
      assert(matchWildcard("animals/*/cats/>", "animals/wild/cats/leopard"))
      assert(
        !matchWildcard("animals/*/cats/>", "animals/domestic/dogs/beagles"))
    }

    it("my/test/*") {
      assert(matchWildcard("my/test/*", "my/test/topic"))
      assert(!matchWildcard("my/test/*", "my/test"))
    }
  }
}
