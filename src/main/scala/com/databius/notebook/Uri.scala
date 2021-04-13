package com.databius.notebook

import java.net.URI

object Uri extends App {
  val uri = new URI("http://user:password@host:123/path#fragment")
  println(uri.getScheme)
  println(uri.getAuthority)
  println(uri.getFragment)
  println(uri.getHost)
  println(uri.getPath)
  println(uri.getPort)
  println(uri.getQuery)
  println(uri.getUserInfo)
}
