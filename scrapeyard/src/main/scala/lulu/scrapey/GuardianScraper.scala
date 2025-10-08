package lulu.scrapey

import org.jsoup.Jsoup

import scala.collection.JavaConverters.asScalaBufferConverter

case class Headline(title: String, url: String)


object GuardianScraper {
  val url = "https://www.theguardian.com"
  val contentSelector = "div[id *= container-]>ul>li a"
  val interestingSection = List("world", "football")
  def scrapeHeadline(section:String): List[Headline] = {
    Jsoup.connect(s"$url/$section").get()
      .select(contentSelector)
      .asScala
      .toList
      .map {link =>
        val title = if link.text.isEmpty then link.attr("aria-label") else link.text()
        Headline(title, link.attr("href"))
      }
  }

  def main(args: Array[String]): Unit = {
    scrapeHeadline("world").foreach(println)
  }
}
