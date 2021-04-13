package com.databius.notebook

/**
  * Template for custom analysis code.
  */
object HelloWorld extends App {
  println("Hello")
  Map(1 -> 2).filterKeys(_ < 0).max
}
