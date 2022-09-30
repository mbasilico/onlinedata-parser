package tech.onlinedata.parser.utils

import org.apache.http.impl.client.{CloseableHttpClient, HttpClients}
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager

object HttpClientUtils {


  val cm = new PoolingHttpClientConnectionManager
  // Increase max total connection to 200
  cm.setMaxTotal(200)
  // Increase default max connection per route to 20
  cm.setDefaultMaxPerRoute(20)

  val httpClient: CloseableHttpClient = HttpClients.custom()
    .setConnectionManager(cm)
    .build()

  def getHttpClient: CloseableHttpClient = httpClient

}
