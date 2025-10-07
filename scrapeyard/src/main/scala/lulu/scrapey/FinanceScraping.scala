package lulu.scrapey

import org.jsoup.Jsoup

import java.io.PrintWriter
import java.time.{LocalDate, ZoneOffset}
import scala.io.Source
import scala.jdk.CollectionConverters.*

object FinanceScraping {
  def escapeCsv(field: String): String = "\"" + field.replace("\"", "\"\"") + "\""

  def getFinanceData():Unit ={
    val mainDoc= Jsoup.connect("https://finance.yahoo.com/quote/AUDUSD%3DX/history/").userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36")
      .timeout(10000).get()
    val table = mainDoc.select("table.table").first()

    val headers = table.select("thead th").asScala.map(_.text())
    println(s"Headers: ${headers.mkString(", ")}")

    val rows= table.select("tbody tr").asScala.map{
      tr => tr.select("td").asScala.map(_.text())
    }

    val today = LocalDate.now()
    val csvFile = new PrintWriter(s"${today}.csv")
    csvFile.println(headers.map(escapeCsv).mkString(","))
    rows.foreach{ row =>
      csvFile.println(row.map(escapeCsv).mkString(","))
    }
    csvFile.close()
    println(s"CSV done")
  }
  
  def fromApi():Unit={
    val symbol = "AUDUSD=X"

    // 今天
    val endDate = LocalDate.now()
    // 一個月前
    val startDate = endDate.minusMonths(1)

    // 轉成 UNIX timestamp
    val period1 = startDate.atStartOfDay().toEpochSecond(ZoneOffset.UTC)
    val period2 = endDate.atStartOfDay().toEpochSecond(ZoneOffset.UTC)

    // CSV 下載 URL
    val url = s"https://query1.finance.yahoo.com/v7/finance/download/$symbol?period1=$period1&period2=$period2&interval=1d&events=history"

    // 下載 CSV
    val csvData = Source.fromURL(url).mkString

    // 儲存檔案
    val fileName = s"${symbol}_${endDate}.csv"
    val writer = new PrintWriter(fileName)
    writer.write(csvData)
    writer.close()

    println(s"CSV 檔案已儲存: $fileName")
  }
  def main(args: Array[String]): Unit = {
    fromApi()
  }

}
