package tech.onlinedata.parser.managers

import de.l3s.boilerpipe.extractors.ArticleExtractor
import org.apache.tika.language.LanguageIdentifier
import org.apache.tika.metadata.{Metadata, TikaCoreProperties}
import org.apache.tika.parser.html.BoilerpipeContentHandler
import org.apache.tika.parser.{AutoDetectParser, ParseContext}
import org.apache.tika.sax.WriteOutContentHandler
import org.xml.sax.ContentHandler
import play.api.Logger
import tech.onlinedata.parser.model.{Content, NewContent}
import tech.onlinedata.parser.utils.StringUtils

import java.io.{ByteArrayOutputStream, OutputStream, PrintWriter, StringWriter}
import java.net.URL
import java.sql.Timestamp
import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ParserManager {
  val logger: Logger = Logger(this.getClass)

  private val MIN_WORDS_COUNT = StringUtils.AVERAGE_READING_SPEED / 2

  private def getBodyStringHandler: (ContentHandler, OutputStream) = {
    val xSw = new ByteArrayOutputStream()
    val handler1 = new WriteOutContentHandler(xSw)
    val textHandler = new BoilerpipeContentHandler(handler1, ArticleExtractor.getInstance())

    (textHandler, xSw)
  }

  /**
   * Removes title from the first paragraph (first 15% of the article) from the body.
   * The main goal is to avoid the reading of multiple times the article's title.
   *
   * @return a new body
   */
  private def removeTitleFromBody(title: String, body: String): String = {
    var newBody = body
    var maxIndexToMatch: Int = newBody.length / 6
    var titleIndex = newBody.indexOf(title)
    while (titleIndex > 0 && (titleIndex < maxIndexToMatch)) {
      newBody = newBody.replaceFirst(title, "")
      maxIndexToMatch = newBody.length / 6
      titleIndex = newBody.indexOf(title)
    }

    newBody
  }

  def getContentFromUrl(pUrl: String): Future[Option[Content]] = {
    try {
      logger.info(s"method: getContentFromUrl. stage: STARTED. pUrl: $pUrl.")
      val startTime = System.currentTimeMillis()

      val metadata = new Metadata()
      val parser = new AutoDetectParser()
      val context = new ParseContext()

      // 1. Parse just the Body text content from the Full HTML Document String
      val urlStream = new URL(pUrl).openStream()

      val (bodyHandler, xSwBody) = this.getBodyStringHandler

      Future {
        parser.parse(urlStream, bodyHandler, metadata, context)

        val xTitle = metadata.get(TikaCoreProperties.TITLE)
        if (xTitle.isEmpty) {
          throw new RuntimeException("Content title is empty.")
        }

        val xBodyTotal = StringUtils.normalizeString(xSwBody.toString)
        val languageIdentifier = new LanguageIdentifier(xBodyTotal)
        val language = languageIdentifier.getLanguage
        if (!StringUtils.isValidLanguage(language)) {
          throw new RuntimeException(s"Content language not valid: $language.")
        }

        val xBody = this.removeTitleFromBody(xTitle, xBodyTotal)

        // 3. Calculates language, wordsCount and readingTime
        val wordsCount: Int = StringUtils.calculateWordCount(xBody)
        if (wordsCount < MIN_WORDS_COUNT) {
          throw new RuntimeException(s"Content body too shorter: $wordsCount words")
        }

        // 4. Calculates endTime, logs Finished OK and return
        val endTime: Long = System.currentTimeMillis()
        val elapsedTime = endTime - startTime
        logger.info(s"method: getContentFromUrl. stage: FINISHED. pUrl: $pUrl. " +
          s"language: $language. elapsedTime: $elapsedTime.")

        val newContent = Content(
          url = pUrl,
          title = Some(xTitle),
          body = Some(xBody),
          wordCount = Some(wordsCount),
          language = Some(language),
          createdDate = Some(Timestamp.from(Instant.ofEpochMilli(System.currentTimeMillis()))))

        Some(newContent)
      }
    } catch {
      case e: Exception =>
        logger.error(s"method: getContentFromUrl. stage: FINISHED_BY_ERROR. pUrl: $pUrl. message: ${e.getMessage}.")

        val sw = new StringWriter
        e.printStackTrace(new PrintWriter(sw))
        logger.error(sw.toString)

        Future {
          None
        }
    }
  }

  def parseContent(newUserContent: NewContent): Option[NewContent] = {
    val contentOption = this.getContentFromUrl(newUserContent.url)
    if (contentOption.isDefined) {
      val content = contentOption.get
      newUserContent.content = Some(content)
      Some(newUserContent)
    } else {
      None
    }
  }
}


object ParserManager {
  val logger: Logger = Logger(this.getClass)
  private var _instance: ParserManager = _

  def instance(): ParserManager = this.synchronized {
    if (_instance == null) {
      logger.info(s"method: instance. stage: FINISHED.")
      _instance = new ParserManager()
    }
    _instance
  }
}
