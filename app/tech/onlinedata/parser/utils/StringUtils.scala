package tech.onlinedata.parser.utils

object StringUtils {
  val AVERAGE_READING_SPEED = 240 // 240 words per minute
  private val validLanguages = List("en", "es")
  private val NOUNS_CHARACTERS_REG_EXP = "[^\\p{IsLatin}]+"
  private val ALPHABET_CHARACTERS_REG_EXP = "[^ÁÉÍÓÚÑáéíóúüña-zA-Z_0-9\\s\\.\\@\\?\\!\\¿\\-\\,\\:]"

  def isValidLanguage(language: String): Boolean = {
    validLanguages.contains(language)
  }

  def normalizeString(pStr: String): String = {
    pStr.replaceAll(ALPHABET_CHARACTERS_REG_EXP, "")
  }

  def normalizeNoun(noun: String): String = {
    noun.toLowerCase().replaceAll(NOUNS_CHARACTERS_REG_EXP, "")
  }

  def isValidNoun(language: String, noun: String): Boolean = {
    if (2 to 16 contains noun.length()) {
      val isStopword = StopWordsUtils.instance().isStopword(language, noun)

      return !isStopword
    }

    false
  }

  def calculateWordCount(text: String): Int = {
    val wordsCount: Int = text.split("\\s+").length
    wordsCount
  }
}
