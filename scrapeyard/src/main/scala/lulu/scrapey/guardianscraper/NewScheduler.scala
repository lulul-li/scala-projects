package lulu.scrapey.guardianscraper

import org.quartz.*
import org.quartz.impl.StdSchedulerFactory
object NewScheduler {
  private val myGroup = "newSchedulerGroup"
  def main(args: Array[String]): Unit = {
    val scheduler = StdSchedulerFactory.getDefaultScheduler
    scheduler.start()

    val job= JobBuilder.newJob(classOf[NewService])
      .withIdentity("newsService",myGroup)
      .build()

    val trigger = TriggerBuilder
      .newTrigger().withIdentity("newSchedulerTrigger", myGroup)
      .startNow().withSchedule(
        SimpleScheduleBuilder.simpleSchedule().withIntervalInMinutes(5).repeatForever()
      ).build()

    scheduler.scheduleJob(job, trigger)
  }
}
