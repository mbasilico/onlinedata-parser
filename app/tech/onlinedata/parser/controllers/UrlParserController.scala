package tech.onlinedata.parser.controllers

import org.apache.http.impl.client.CloseableHttpClient
import play.api.libs.json.Json
import play.api.{Configuration, Logger}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import tech.onlinedata.parser.managers.{NounsManager, ParserManager}
import tech.onlinedata.parser.utils.{HttpClientUtils, URLUtils}

import javax.inject._
import scala.concurrent._
import ExecutionContext.Implicits.global


@Singleton
class UrlParserController @Inject()(cc: ControllerComponents, config: Configuration) extends AbstractController(cc) {

  val logger: Logger = Logger(this.getClass)
  val httpClient: CloseableHttpClient = HttpClientUtils.getHttpClient

  def parseUrl: Action[AnyContent] = Action.async { implicit request => {
    val requestBody = request.body.asText
    requestBody match {
      case Some(anUrl) =>
        if (!URLUtils.isValid(anUrl)) Future.successful(BadRequest)
        else {
          val parserManager = ParserManager.instance()
          val futureContentOpt = parserManager.getContentFromUrl(anUrl)
          val result = for {
            contentOpt <- futureContentOpt
            res <- contentOpt match {
              case Some(content) =>
                NounsManager.instance().calculateNouns(content)
                Future.successful(Ok(Json.toJson(content)))
              case None =>
                logger.warn(s"BadRequest couldn't get URL data for: $anUrl")
                Future.successful(BadRequest)
            }
          } yield res

          result
        }
      case _ =>
        logger.warn(s"BadRequest with requestBody: $requestBody")
        Future.successful(BadRequest)
    }
  }
  }
}
