package lulu.scrapey

import java.util.concurrent.Executors
import scala.annotation.tailrec
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}

object ParallelCrawlers {
  def fetchLinks(title: String): List[String] = {
    @tailrec
    def fetchLinksRec(continue: Option[Map[String, String]], acc: List[String], stopLength: Int = 20): List[String] = {
      if acc.length > stopLength then acc
      else
        continue match {
          case None => acc
          case Some(value) => {
            println(s"Current link: $acc")
            val resp = requests.get("http://en.wikipedia.org/w/api.php", params = Map(
              "action" -> "query",
              "titles" -> title,
              "prop" -> "links",
              "format" -> "json") ++ value
            )
            val newLink = for {
              page <- ujson.read(resp)("query")("pages").obj.values.toList
              links <- page.obj.get("links").toList.filter(!_.isNull)
              link <- links.arr
            } yield link("title").str

            val newContinue = ujson.read(resp).obj.get("continue").map(_.obj.view.mapValues(_.str).toMap)
            fetchLinksRec(newContinue, acc ++ newLink)
          }
        }
    }

    fetchLinksRec(Some(Map()), List())
  }

  //(using ExecutionContext)：表示這個函數需要一個「執行緒池上下文（ExecutionContext）」來執行並行的 Future。
  def fetchAllLinksPar(startTitle: String, depth: Int = 2)(using ExecutionContext): List[String] = {
    @tailrec
    def bfs(level: Int = 0, seen: Set[String] = Set(), current: List[String] = List()): List[String] = {
      if level >= depth then seen.toList
      else {
        val futures = current.map(title => Future(fetchLinks(title)))

        //組成一個 Future[List[List[String]]]。
        val bigFuture = Future.sequence(futures).map(_.flatten)
        val nextTitles = Await.result(bigFuture, 1.minute)
        bfs(level + 1, seen ++ nextTitles, nextTitles)
      }
    }

    bfs(0, Set(startTitle), List(startTitle))
  }

  def main(args: Array[String]): Unit = {
    given ExecutionContext = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(8))

    fetchAllLinksPar("Bucharest").foreach(println)
  }
}
