package chat.domain

import upickle.default.*

case class Message(
                    id: String,
                    sender: String,
                    content: String,
                    parentid: Option[String],
                    timestamp: Long //unix time
                  ) derives ReadWriter
