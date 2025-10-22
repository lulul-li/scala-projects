package chat

import chat.domain.Message
import scalasql.*
import scalasql.simple.{DbClient, SimpleTable}
import scalasql.dialects.PostgresDialect.*

import java.sql.{Driver, DriverManager}

object ChatDb {
  object Message extends SimpleTable[Message]

  val dbClient = new DbClient.Connection(
    DriverManager.getConnection("jdbc:postgresql://localhost:54321/", "docker", "docker"),
  )
  
  val db = dbClient.getAutoCommitClientConnection

  def getAllMessages():List[Message] = {
   val mgs= db.run(Message.select)
    mgs.toList
  }
  def getMessagesByUser(user:String):List[Message] = {
    db.run(Message.select.filter(_.sender === user)).toList
  }
  def saveMessage(message:Message):Unit = {
  db.run(
    Message.insert.columns(
      _.id := message.id,
      _.sender := message.sender,
      _.content := message.content,
      _.parentid := message.parentid,
      _.timestamp := message.timestamp,
    ))
  }

}
