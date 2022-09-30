package tech.onlinedata.parser.clients

import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.utils.URIBuilder
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import play.api.Logger
import play.api.libs.json.Json
import tech.onlinedata.parser.config.Config
import tech.onlinedata.parser.utils.StringUtils

import java.io.ByteArrayOutputStream
import java.net.URLEncoder
import java.util.Base64
import scala.collection.Seq

object CoreNLPClient {
  private val logger = Logger(this.getClass)

  private val NOUN_PATTERNS_BY_LANGUAGE: Map[String, String] = Map(
    "en" -> "[{pos:NN}] || [{pos:NNP}] || [{pos:NNS}]",
    "es" -> "[{pos:NOUN}]")

  private val CORENLP_URL_BY_LANGUAGE: Map[String, String] = Map(
    "en" -> Config.CORENLP_EN_URL,
    "es" -> Config.CORENLP_ES_URL)

  private val CORENLP_ANNOTATORS = "tokenize,ssplit,pos"

  private def getResponseString(response: HttpResponse): String = {
    val out = new ByteArrayOutputStream()
    response.getEntity.writeTo(out)
    val responseString = out.toString()
    out.close()

    responseString
  }

  def getNounsFromResponse(response: HttpResponse, language: String): Seq[String] = {
    val responseString = this.getResponseString(response)

    val jsonObject = Json.parse(responseString)
    val nouns = jsonObject \\ "text"
    val normalizedNouns = nouns.map(result => StringUtils.normalizeNoun(result.toString()))
    val validNouns = normalizedNouns.filter(normalizedNoun => StringUtils.isValidNoun(language, normalizedNoun))

    validNouns
  }

  // Invoke coreNLP service:
  // Search for Nouns:
  // http://localhost:9001/tokensregex?pattern=[{pos:NN}] || [{pos:NNP}] || [{pos:NNS}]&properties={"annotators": "tokenize,ssplit,pos"}&pipelineLanguage=en
  def callCoreNLP(contentBody: String, language: String, client: CloseableHttpClient): HttpResponse = {
    val startTime = System.currentTimeMillis()

    val jsonParam = Json.obj("annotators" -> CORENLP_ANNOTATORS, "outputFormat" -> "json")
    val corenlpUrl = CORENLP_URL_BY_LANGUAGE(language)
    val nounPatternParam = NOUN_PATTERNS_BY_LANGUAGE(language)

    logger.info(s"method: callCoreNLP. stage: STARTED. corenlpUrl: $corenlpUrl. language: $language.")

    val builder = new URIBuilder(corenlpUrl)
    builder.setParameter("pattern", nounPatternParam)
      .setParameter("properties", jsonParam.toString())
      .setParameter("pipelineLanguage", language)

    val httpPost = new HttpPost(builder.build())

    val encodedBody = URLEncoder.encode(contentBody, "UTF-8")
    val entity = new StringEntity(encodedBody)
    httpPost.setEntity(entity)

    httpPost.setHeader("Content-type", "application/json; charset=UTF-8")
    val usernamePassword = Config.CORENLP_USERNAME + ":" + Config.CORENLP_PASSWORD
    val encoding = Base64.getEncoder.encodeToString(usernamePassword.getBytes())
    httpPost.setHeader("Authorization", "Basic " + encoding)

    val response = client.execute(httpPost)

    val endTime = System.currentTimeMillis()
    val elapsedTime = endTime - startTime
    logger.info(s"method: callCoreNLP. stage: FINISHED. corenlpUrl: ${corenlpUrl}. language: ${language}. elapsedTime: ${elapsedTime}.")

    response
  }
}
