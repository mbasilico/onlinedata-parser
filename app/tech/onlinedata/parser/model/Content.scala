package tech.onlinedata.parser.model

import play.api.libs.json.{Json, OFormat, Reads, Writes}

import java.sql.Timestamp

case class Content( //var _id: Option[BSONObjectID] = None,
                    url: String,
                    var createdDate: Option[Timestamp] = None,
                    var title: Option[String] = None,
                    var body: Option[String] = None,
                    var wordCount: Option[Int] = None,
                    var language: Option[String] = None,
                    var keywords: Option[List[String]] = None,
                    var summary: Option[String] = None,
                    var summaryWordCount: Option[Int] = None)

object Content {
  implicit val timestampReads: Reads[Timestamp] = {
    implicitly[Reads[Long]].map(new Timestamp(_))
  }

  implicit val timestampWrites: Writes[Timestamp] = {
    implicitly[Writes[Long]].contramap(_.getTime)
  }
  implicit val archivedDtoFormatter: OFormat[Content] = Json.format[Content]
}