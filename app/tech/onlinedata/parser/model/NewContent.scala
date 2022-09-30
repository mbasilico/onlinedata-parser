package tech.onlinedata.parser.model

import java.sql.Timestamp

case class NewContent(url: String,
                      shareCount: Long,
                      likeCount: Long,
                      interesting: Boolean,
                      var content: Option[Content] = None,
                      var createdDate: Option[Timestamp] = None)
