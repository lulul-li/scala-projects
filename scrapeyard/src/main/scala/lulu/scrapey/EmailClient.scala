package lulu.scrapey

import java.util.{Properties, UUID}
import javax.mail.internet.{InternetAddress, MimeMessage}
import javax.mail.{Authenticator, Message, PasswordAuthentication, Session, Transport}

object EmailClient {
  val user = "piper.altenwerth@ethereal.email"
  val password = "MA8tbYUtfznAkDE9Wh"
  val host = "smtp.ethereal.email"
  val port = "587"

  def sendEmail(to: String, subject: String, body: String) = {
    // 1 - session
    val props = new Properties()
    props.put("mail.smtp.host", host)
    props.put("mail.smtp.port", port)
    props.put("mail.smtp.auth", true)
    props.put("mail.smtp.starttls.enable", true)
    val session = Session.getInstance(props, new Authenticator {
      override def getPasswordAuthentication: PasswordAuthentication =
        new PasswordAuthentication(user, password)
    })
    // 2 - message

    val msg = new MimeMessage(session)
    msg.setFrom(new InternetAddress("no-reply@ulu.com"))
    msg.setRecipients(Message.RecipientType.TO, to)
    msg.setSubject(subject)
    msg.setContent(body, "text/html")
    msg.setHeader("Message-ID", UUID.randomUUID().toString)
    // 3 - send the message

    try {
      Transport.send(msg)
      println("Email sent successfully")
    }
    catch {
      case e: Exception =>
        e.printStackTrace()
    }
  }

  def main(args: Array[String]): Unit = {
    sendEmail("user@email.com", "First email",
      """
        |<div>
        |hello
        |</div>
        |""".stripMargin)
  }
}
