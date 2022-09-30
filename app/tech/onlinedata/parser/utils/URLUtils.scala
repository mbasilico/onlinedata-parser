package tech.onlinedata.parser.utils

object URLUtils {

  def isValid(url: String) = url.startsWith("http://") || url.startsWith("https://")
}
