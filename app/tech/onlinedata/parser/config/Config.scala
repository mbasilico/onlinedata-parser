package tech.onlinedata.parser.config

import com.typesafe.config.ConfigFactory

object Config {
  val config = ConfigFactory.load()
  val CORENLP_ES_URL = config.getString("corenlp.es.url")
  val CORENLP_EN_URL = config.getString("corenlp.en.url")
  val CORENLP_USERNAME = config.getString("corenlp.username")
  val CORENLP_PASSWORD = config.getString("corenlp.password")
  val CORENLP_WAKEUP = if (config.hasPath("corenlp.wakeup")) config.getBoolean("corenlp.wakeup") else false
}