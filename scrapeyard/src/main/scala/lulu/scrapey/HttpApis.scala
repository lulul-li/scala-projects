package lulu.scrapey

object HttpApis {
  def main(args: Array[String]): Unit = {
    val firstResponse = requests.get("https://api.github.com/users/lulul-li")
    println(firstResponse.statusCode)
    println(firstResponse.text())
    val json= ujson.read(firstResponse.text())
    println(json.obj.keySet)

    val r1 = requests.post("http://httpbin.org/post", params = Map("user"-> "daniel", "password" -> "rockthejvm"))
    println(r1.text())

    val r2 = requests.put("http://httpbin.org/put", data = Map("user"-> "daniel", "password" -> "rockthejvm"))
    println(r2.text())
  }
}
