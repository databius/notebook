package com.databius.notebook.solace

import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.impl.client.{BasicCredentialsProvider, HttpClientBuilder}
import org.json4s.JValue
import org.json4s.jackson.JsonMethods
import org.json4s.jackson.JsonMethods._

object HttpGetTester {

  def main(args: Array[String]) {
    val url = "http://localhost:8080/SEMP/v2/config/msgVpns?select=msgVpnName"
    val authScope = new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT)
    val credentials =
      new UsernamePasswordCredentials("red_user", "red_user_password")
    val provider = new BasicCredentialsProvider
    provider.setCredentials(authScope, credentials)

    // Add AuthCache to the execution context// Add AuthCache to the execution context
    val context = HttpClientContext.create
    context.setCredentialsProvider(provider)

    val client = HttpClientBuilder.create().build()
    val httpRequest = new HttpGet(url)

    val httpResponse = client.execute(httpRequest, context)
    val entity = httpResponse.getEntity

    val content: JValue = JsonMethods.parse(entity.getContent())
    println(compact(render(content)))
  }
}
