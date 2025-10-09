package lulu.scrapey.guardianscraper

import lulu.scrapey.guardianscraper.EmailClient
import org.quartz.{Job, JobExecutionContext}

import java.time.LocalDateTime

class NewService extends Job{
  override def execute(context: JobExecutionContext): Unit = {
    println("Job Execute...")
    execute(
      List("user1@email.com", "user2@email.com")
    )
  }

  def execute(subscribers: List[String]): Unit = {
    val headlinesSection = GuardianScraper.interestingSection.map { section =>
      val headlines = GuardianScraper.scrapeHeadline(section)
      val sectionTags = headlines.map(h => s"<li><a href=\"${h.url}\">${h.title}</a><li>")
      sectionTags.mkString(s"<h2>${section}</h2><div><ul>", "\n", "</ul></div>")
    }

    val emailContent = {
      s"""
         |<h1> Guardian Scraper - news<h1>
         |<div>
         |${headlinesSection.mkString("<br/><br/>")}
         |</div>
         |""".stripMargin
    }
    subscribers.foreach { address =>
      println("fetch data done, get mail client....")
      EmailClient.default.sendEmail(address, s"Guardian scraper -${LocalDateTime.now()}", emailContent)
    }
  }

}
