import chat.domain.Message
import org.scalajs.dom
import org.scalajs.dom.{MessageEvent, WebSocket, console, html}

import scala.scalajs.js
import upickle.default.*
import scalatags.JsDom.all.*

import scala.util.Random

object ChatApp extends App {
  private val nameInput = dom.document.querySelector("header input").asInstanceOf[html.Input]
  private val chantContainer = dom.document.querySelector(".chat-container").asInstanceOf[html.Div]
  private val colors = List("#e6194b", "#3cb44b", "#ffe119", "#4363d8", "#f58231", "#911eb4", "#46f0f0", "#f032e6")
  private val chatInput = dom.document.querySelector("footer input").asInstanceOf[html.Input]

  private def wsUrl(name: String) = s"ws://localhost:8080/subscribe/$name"

  //mutable variables
  private var username: Option[String] = None
  private var ws: Option[WebSocket] = None
  private var replyTo: Option[String] = None
  private val userColors = collection.mutable.Map[String, String]()

  private def assignColor(user: String): String = {
    userColors.getOrElseUpdate(user, colors(Random.nextInt(colors.length)))
  }

  def connectWebSocket(name: String) = {
    val newws = new WebSocket(wsUrl(name))
    newws.onmessage = { (e: MessageEvent) =>
      val data = e.data.toString
      val message = read[Message](data)
      renderMessage(message)
    }
    ws = Some(newws)
    dom.window.onbeforeunload = _ => newws.close()
  }

  def setupEvents(): Unit = {
    nameInput.onkeydown = { (e: dom.KeyboardEvent) =>
      if (e.key == "Enter" && nameInput.value.trim.nonEmpty) {
        val name = nameInput.value.trim
        username = Some(name)
        nameInput.disabled = true
        dom.console.log(s"Logged in as $name")
        connectWebSocket(name)
      }
    }

    chatInput.onkeydown = { (e: dom.KeyboardEvent) =>
      console.log("Key pressed in chat input:", e.key)
      if (e.key == "Enter" && chatInput.value.trim.nonEmpty && username.isDefined) {
        console.log("Enter")
        val content = chatInput.value.trim
        val message = Message(
          id = "",
          sender = username.get,
          content = content,
          parentid = None,
          timestamp = js.Date.now().toLong
        )
        ws.foreach { socket =>
          socket.send(write(message))
        }
        chatInput.value = ""
        val preview = dom.document.querySelector(".input-reply-preview")
        if (preview != null) {
          preview.parentNode.removeChild(preview)
        }
        replyTo = None

      }
    }
  }

  def renderSystemMessage(message: Message) = {
    chantContainer.appendChild(
      div(
        cls := "system-message",
        message.content
      ).render
    )
  }

  def renderUserMessage(message: Message) = {
    val date = new js.Date(message.timestamp)
    val timeString = date.toLocaleTimeString()
    val baseCssClasses = {
      if (message.sender == username.getOrElse("")) {
        List("message", "self")
      } else {
        List("message", "other")
      }
    }
    val messageDiv = div(
      cls := baseCssClasses.mkString(" "),
      attr("data-id") := message.id,
      div(b(s"${message.sender}")),
      div(cls := "message-content", message.content),
      div(
        cls := "meta",
        timeString + " ",
        span(
          cls := "reply-link", onclick := { (e: dom.Event) =>
            val target = e.target.asInstanceOf[html.Element]
            val messageDiv = target.closest(".message").asInstanceOf[html.Div]
            replyTo = Option(messageDiv.getAttribute("data-id"))
            dom.console.log(s"Replying to message ID: $replyTo")

            val sender = messageDiv.querySelector("b").textContent
            val content = messageDiv.querySelector(".message-content").textContent
            val previewText = s"$sender: $content"
            dom.console.log("Preview text:", previewText)

            var preview = dom.document.querySelector(".input-reply-preview")
            if (preview == null) {
              preview = div(cls := "input-reply-preview", previewText).render
              chatInput.parentNode.insertBefore(preview, chatInput)
            }
            else {
              preview.textContent = previewText
            }

            chatInput.focus()
          }, " Reply")
      )
    ).render
    if (message.sender != username.getOrElse("")) {
      messageDiv.style.background = assignColor(message.sender)
    }
    chantContainer.appendChild(messageDiv)
    chantContainer.scrollTop = chantContainer.scrollHeight
  }

  def renderMessage(message: Message) = {
    if (message.sender == "SYSTEM") {
      renderSystemMessage(message)
    }
    else renderUserMessage(message)
  }

  setupEvents()
  dom.console.log("Hello from ChatApp!")
}
