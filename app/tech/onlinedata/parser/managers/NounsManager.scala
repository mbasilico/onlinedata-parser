package tech.onlinedata.parser.managers

import play.api.Logger
import tech.onlinedata.parser.clients.CoreNLPClient
import tech.onlinedata.parser.model.Content
import tech.onlinedata.parser.utils.{HttpClientUtils, StringUtils}

import java.io.{PrintWriter, StringWriter}

/**
 * Calculates the keywords from a Content object.
 * It connects to CoreNLP service and select the best nouns
 * that apply to that Content
 */
class NounsManager {
  private val logger = Logger(this.getClass)
  private val HIGHEST_NOUNS = 20
  private val TITLE_MULTIPLIER = 2 // If the keyword is present in Title then we multiply by TITLE_MULTIPLIER

  private def calculateFrecuency(nouns: List[String]): Map[String, Int] = {
    val nounsFrecuency = scala.collection.mutable.Map[String, Int]()

    for (currentKeyword <- nouns) {
      if (!nounsFrecuency.contains(currentKeyword)) nounsFrecuency(currentKeyword) = 0
      nounsFrecuency(currentKeyword) += 1
    }

    nounsFrecuency.toMap
  }

  // If the keyword is present in the Title is far more important!! multiply by 4
  private def applyTitleMultiplier(nounsFrecuency: Map[String, Int], title: String): Map[String, Int] = {
    val nounsFrecuencyWithMultiplier = scala.collection.mutable.Map[String, Int]()

    nounsFrecuencyWithMultiplier ++= nounsFrecuency

    val titleWords = title.split("\\s+")
    for (currenTitleWord <- titleWords) {
      val normalizedTitleWord = StringUtils.normalizeNoun(currenTitleWord)
      if (nounsFrecuency.contains(normalizedTitleWord)) nounsFrecuencyWithMultiplier(normalizedTitleWord) *= TITLE_MULTIPLIER
    }
    nounsFrecuencyWithMultiplier.toMap
  }

  private def selectBestNouns(nounsFrecuency: Map[String, Int], title: String, takeCount: Int = HIGHEST_NOUNS): List[String] = {

    val nounsFrecuencyWithMultiplier = this.applyTitleMultiplier(nounsFrecuency, title)

    // Order descending (highest values first) by value
    val nounsByValue = nounsFrecuencyWithMultiplier.toSeq.sortBy(-_._2)

    // Take the takeCount number more frecuent
    val bestNounsList = nounsByValue.take(takeCount).map(tuple => tuple._1).toList
    val bestNounListSize = bestNounsList.size

    logger.info(s"method: selectBestNouns. stage: FINISHED. bestNounsList.size: $bestNounListSize.")

    bestNounsList
  }

  def calculateNouns(title: String, body: String, language: String): List[String] = {
    val startTime = System.currentTimeMillis()
    val client = HttpClientUtils.getHttpClient

    logger.info(s"method: calculateNouns. stage: STARTED. language: $language.")
    if (!StringUtils.isValidLanguage(language)) {
      logger.warn(s"method: calculateNouns. stage: FINISHED_BY_WARN. language: $language. message: Not a valid language")
      return List.empty[String]
    }

    try {
      val response = CoreNLPClient.callCoreNLP(body, language, client)
      val statusCode = response.getStatusLine.getStatusCode

      if (statusCode == 200) {
        val validNouns = CoreNLPClient.getNounsFromResponse(response, language)
        val nounsFrecuency = this.calculateFrecuency(validNouns.toList)
        // Select Best Nouns
        val bestNounsList = this.selectBestNouns(nounsFrecuency, title).toSet
        val bestNounsFrecuencyMap = nounsFrecuency.filter(pred => bestNounsList.contains(pred._1))

        client.close()

        val endTime = System.currentTimeMillis()
        val elapsedTime = endTime - startTime
        logger.info(s"method: calculateNouns. stage: FINISHED. language: $language. elapsedTime: $elapsedTime. message: bestNounsFrecuencyMap OK.")

        this.selectBestNouns(bestNounsFrecuencyMap, title)
      } else {
        logger.error(s"method: calculateNouns. stage: FINISHED_BY_ERROR. language: $language. statusCode: $statusCode.")
        client.close()

        List.empty[String]
      }
    } catch {
      case e: Exception =>
        logger.error(s"method: calculateNouns. stage: FINISHED_BY_ERROR. language: $language. message: ${e.getMessage}.")

        val sw = new StringWriter
        e.printStackTrace(new PrintWriter(sw))
        logger.error(sw.toString)

        client.close()

        List.empty[String]
    }
  }

  def calculateNouns(content: Content): Unit = {
    val body = content.body.get
    val language = content.language.get
    val title = content.title.get

    val bestNouns = this.calculateNouns(title, body, language)
    content.keywords = Some(bestNouns)
  }
}

object NounsManager {
  val logger: Logger = Logger(this.getClass)
  private var _instance: NounsManager = _

  def instance(): NounsManager = this.synchronized {
    if (_instance == null) {
      logger.info(s"method: instance. stage: FINISHED.")
      _instance = new NounsManager()
    }
    _instance
  }
}
