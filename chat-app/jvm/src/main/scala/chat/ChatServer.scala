package chat

import cask.{MainRoutes, Ws, WsActor}
import cask.endpoints.{WsChannelActor, WsHandler, getJson, staticFiles, websocket}
import chat.domain.{ErrorMessage, Message}
import upickle.default.*

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import scala.collection.mutable.ArrayBuffer

object ChatServer extends MainRoutes {
  private val SYSTEM = "SYSTEM"
  private val db = ChatDb
  private val wsConnections = ConcurrentHashMap.newKeySet[WsChannelActor]()

  // GET /messages => List[Message]
  @getJson("/messages")
  def retreieveAllMessages() = db.getAllMessages()

  // GET /messages/search/:user => List[Message] sent by that user
  @getJson("/messages/search/:user")
  def queryMessagesByUser(user: String) = db.getMessagesByUser(user)

  // POST /chat {sender, content, parent,timestamp} => Message that has just been sent
  @cask.postJson("/chat")
  def sendMessage(sender: String, content: String, parentId: Option[String] = None, timestamp: Long = -1L) = {
    if (sender.trim.isEmpty) writeJs(ErrorMessage("Sender cannot be empty"))
    else if (content.trim.isEmpty) writeJs(ErrorMessage("Content cannot be empty"))
    else {
      val message = Message(
        java.util.UUID.randomUUID().toString,
        sender, content, parentId, if (timestamp > 0) timestamp else System.currentTimeMillis()
      )
      db.saveMessage(message)

      wsConnections.forEach(_.send(Ws.Text(write(message))))
      writeJs(message)
    }

  }

  /*
  WebSocket /subscribe/:user => real-time chat
    - receive new messages
    - broadcast messages to all connected clients
    - send a message
   */
  @websocket("/subscribe/:user")
  def subscribe(user: String) = WsHandler { channel =>
    // retrieve all messages
    db.getAllMessages().foreach(m => channel.send(Ws.Text(write(m))))
    // save the connection
    wsConnections.add(channel)
    // broadcast new messages to all connected clients
    wsConnections.forEach(_.send(Ws.Text(write(Message(
      java.util.UUID.randomUUID().toString,
      SYSTEM, s"$user has joined the chat", None, System.currentTimeMillis()
    )))))

    // return an actor that will
    WsActor {
      case event@Ws.Text(text) =>
        val msg = read[Message](text)
        // check the sender
        if (msg.sender != user) {
          channel.send(Ws.Text(write(
            Message(
              id = UUID.randomUUID().toString,
              sender = SYSTEM,
              content = s"Error: somehow the sender is not the same as the user being subscribed.",
              parentid = None,
              timestamp = System.currentTimeMillis()
            )
          )))
        } else {
          // set the id of the message in case the id is empty
          val completeMsg =
            if (msg.id.isEmpty)
              msg.copy(id = UUID.randomUUID().toString)
            else
              msg

          db.saveMessage(completeMsg)
          wsConnections.forEach(_.send(Ws.Text(write(completeMsg))))
        }
      case Ws.Close(_, _) =>
        wsConnections.remove(channel)
        // handle disconnection
        wsConnections.forEach(_.send(Ws.Text(write(Message(
          java.util.UUID.randomUUID().toString,
          SYSTEM, s"$user has left the chat", None, System.currentTimeMillis()
        )))))
    }
  }

  @staticFiles("/static")
  def serveStaticFiles() = {
    "/Users/lulu/Exercism/scala/projects/chat-app/js/static/index.html"
  }

  initialize()
}
