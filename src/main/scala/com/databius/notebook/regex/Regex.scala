package com.databius.notebook.regex

object Regex extends App {
  val pattern = "([0-9]+) ([A-Za-z]+)".r
  val pattern(count, fruit) = "100 Bananas"

  val topic = "mon/v0/pb/sub/lb/heartbeat/CSQ/csqdev-ems01/babelaxcme-d"
  val wildcard = "mon/v0/pb/sub/lb/heartbeat/CSQ/csqdev-ems01/babel*"

  val matched = wildcard
    .replaceAll("\\*", "[^/]*")
    .replaceAll(">", ".+")
    .r
    .pattern
    .matcher(topic)
    .matches
  println(s"$wildcard matcher $topic ->` $matched")

  println(matchesWildcard(wildcard, topic))
  def matchesWildcard(wildcard: String, topic: String): Boolean = {
    val matched = wildcard
      .replaceAll("\\*", "[^/]*")
      .replaceAll(">", ".+")
      .r
      .pattern
      .matcher(topic)
      .matches
    println(s"$wildcard matcher $topic =>` $matched")
    matched
  }

  def matchWildcard(wildcard: String, topic: String) = {
    val regex = wildcard.replaceAll("\\*", "[^/]*").replaceAll(">", ".+").r
    regex.pattern
      .matcher(topic)
      .matches
  }
}
