package tech.onlinedata.parser.utils

import play.api.{Environment, Logger}

import scala.collection.mutable
import scala.io.Source

class StopWordsUtils {
  val logger: Logger = Logger(this.getClass)
  val supportedLanguages: Seq[String] = List("es", "en")

  val stopwordsByLanguage: Map[String, mutable.HashSet[String]] = Map(
    "es" -> new mutable.HashSet[String](),
    "en" -> new mutable.HashSet[String]())

  logger.info(s"method: LOADING. stage: STARTED.")
  for (currentLanguage <- supportedLanguages) {
    val stopwordsFilename = "stopwords-" + currentLanguage + ".txt"
    logger.info(s"method: LOADING. stage: STARTED. stopwordsFilename: $stopwordsFilename.")
    val stopwordsInputStream = Environment.simple().resourceAsStream(stopwordsFilename).get
    val stopwordsLines = Source.fromInputStream(stopwordsInputStream).getLines()
    val stopwordsLinesCount = stopwordsLines.length
    logger.info(s"method: LOADING. stage: STARTED. stopwordsLinesCount: $stopwordsLinesCount.")

    for (currentStopword <- stopwordsLines) {
      val currentHashSet: mutable.HashSet[String] = stopwordsByLanguage(currentLanguage)
      currentHashSet.add(currentStopword)
    }
    
    stopwordsInputStream.close()
  }
  logger.info(s"method: LOADING. stage: FINISHED.")

  def isStopword(language: String, word: String): Boolean = {
    val stopwords: mutable.HashSet[String] = stopwordsByLanguage(language)

    stopwords.contains(word)
  }
}


object StopWordsUtils {
  val logger: Logger = Logger(this.getClass)
  private var _instance: StopWordsUtils = _

  def instance(): StopWordsUtils = this.synchronized {
    if (_instance == null) {
      logger.warn(s"StopWordsManager created once!?")
      _instance = new StopWordsUtils()
    }
    _instance
  }
}

