package com.databius.notebook.restful

import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient

object GetUrlContent extends App {

  val url = "http://api.hostip.info/get_json.php?ip=12.215.42.19"
  val result = scala.io.Source.fromURL(url).mkString
  println(result)

  def getRestContent(url: String): String = {
    val httpClient = new DefaultHttpClient()
    val httpResponse = httpClient.execute(new HttpGet(url))
    val entity = httpResponse.getEntity()
    var content = ""
    if (entity != null) {
      val inputStream = entity.getContent()
//      content = io.Source.fromInputStream(inputStream).getLines.mkString
      inputStream.close
    }
    httpClient.getConnectionManager().shutdown()
    return content
  }
}
