package librarydemos

import cask.*
import cask.model.Response.Raw
import cask.router.{EndpointMetadata, Result}
import upickle.default.*

import scala.util.Random

object CaskDemo extends MainRoutes {
  // override def port: Int = 1234

  @cask.get("/health")
  def healthRoute() = "OK"

  //path segments
  @cask.get("/hello/:name") // "name" must match the method parameter name
  def parsePathSegment(name: String) = s"Hello, $name!"

  //query parameters
  @get("/search")
  def search(query: String, limit: Int = 10) = s"Searching for '$query' with limit $limit"

  case class Person(name: String, age: Int) derives ReadWriter

  @get("/user/:name")
  def findUser(name:String)= {
    write(Person(name, Random.nextInt(100)))
  }

  @getJson("/user_v2/:name")
  def findUser_v2(name: String) = {
    Person(name, Random.nextInt(100))
  }

  @post("/person")
  def postWithPayload(req:Request)= s"person ${read[Person](req.text())} received"

  //post localhost:8080/person_v2 -- data '{"name":"Alice","age":30}' -H "Content-Type: application/json"
  @postJson("/person_v2")
  def addPerson(name:String, age:Int)= s"person ${Person(name,age)} added"

  //post localhost:8080/person_v3 --data '{"person":{"name":"Alice","age":30}}' -H "Content-Type: application/json"
  @postJson("/person_v3")
  def addPerson_v3(person: Person) = s"person ${person} added"

  @staticFiles("/static")
  def staticFile() = "/Users/lulu/Exercism/scala/projects/chat-app-backend/src/main/scala/librarydemos"

  @websocket("/ws")
  def ws()=cask.WsHandler { channel =>
    cask.WsActor {
      case cask.Ws.Text(msg) =>
        channel.send(cask.Ws.Text(s"Echo: $msg"))
    }
  }

  @get("/fail")
  def fail() = throw new Exception("Something went wrong!")

//  override def handleEndpointError(routes: Routes, metadata: EndpointMetadata[_], e: Result.Error, req: Request): Raw =
//    s"Error occurred: ${e}"
  initialize()
}
