package lulu.scrapey

import org.jsoup.Jsoup
import org.jsoup.nodes.Element

import scala.jdk.CollectionConverters.*
object BasicScraping {

  def getWiki(): Unit = {
    val doc = Jsoup.connect("https://en.wikipedia.org/wiki/Main_Page").get()
    println(doc.title())
    val headlines = doc.select("#mp-itn b a").asScala
    headlines.map(_.attr("title")).foreach(println)
  }

  def getWebApisFromMDN()={
    val mainDoc= Jsoup.connect("https://developer.mozilla.org/en-US/docs/Web/API").get()
    val links= mainDoc.select("h2#interfaces").next.next.select("div.index").select("a").asScala
    val linkData = links.map{ link =>
      (link.attr("href"),link.attr("title"))
    }
    val articles = linkData.take(5).map{
      case (url, title) =>
        println(s"Scraping $title")
        val doc = Jsoup.connect(s"https://developer.mozilla.org$url").get()
        val summary = doc.select("section.content-section > p").asScala.headOption.map(_.text).getOrElse("no description")

        val methodAndProperties= doc.select(".reference-layout__body .content-section dl dt").asScala.map{
          term =>
            val name = term.text
            val description = Option(term.nextElementSiblings()).map(_.text()).getOrElse("no description")

            s"$name $description"
        }

        val report =
          s"""
             |$title - $summary
             |${methodAndProperties.mkString("\n\t")}
             |""".stripMargin

        report
    }

    articles.foreach(println)
  }

  case class Comment(author:String, content:String, replies:List[Comment])

  def parseLobstersDiscussion(url: String): List[Comment] = {
    def recurse(node:Element):Comment= {
      val user = node.select("div.byline a").asScala
        .find(link => Option(link.attr("href")).exists(_.contains("~")))
        .map(link => link.attr("href").substring(1))
        .getOrElse("anonymous")

      val content = node.selectFirst("div.comment_text").text()
      val replies = node.selectFirst("ol.comments").children().asScala.map(recurse).toList
      Comment(user,content,replies)
    }

    val doc = Jsoup.connect("https://lobste.rs/" + url).get()
    doc.select("ol.comments.comments1 ol.comments > li.comments_subtree").asScala
      .map(recurse).toList

  }
  def main(args:Array[String]):Unit={
    parseLobstersDiscussion("s/bu1a84/i_brain_coded_static_image_gallery_few").foreach(println)
  }

}
