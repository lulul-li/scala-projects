package librarydemos

import scalasql.*
import scalasql.simple.{*, given}
import scalasql.dialects.PostgresDialect.*

import java.sql.DriverManager

case class Player(id: String, name: String, email: String, xp: Int, gameType: String)

object Player extends SimpleTable[Player]

case class Game(id: String, name: String, gameType: String)

object Game extends SimpleTable[Game]

object SqlDemo {
  def main(args: Array[String]): Unit = {

    val dbClient = new DbClient.Connection(
      DriverManager.getConnection("jdbc:postgresql://localhost:54321/", "docker", "docker")
    )
    val db = dbClient.getAutoCommitClientConnection

    val basicQuery = Player.select
    val result = db.run(basicQuery)
    //println(result)

    val findByIdQuery = Player.select.filter(_.id === "1")
    //println(db.renderSql(findByIdQuery))
    val result2 = db.run(findByIdQuery)
    // println(result2)

    val highXpQuery = Player.select.filter(_.xp > 1000)
    val result3 = db.run(highXpQuery)
    //println(result3)

    val allName = Player.select.map(_.name)
    //println(db.renderSql(allName))

    val aggregates = Player.select.aggregate(p =>
      (p.minBy(_.xp), p.maxBy(_.xp), p.avgBy(_.xp)))
    //println(db.renderSql(aggregates))
    val groupBy = Player.select.groupBy(_.gameType)(p =>
      (p.minBy(_.xp), p.maxBy(_.xp), p.avgBy(_.xp)))
    // println(db.renderSql(groupBy))

    val ordering = Player.select.sortBy(_.xp).desc
    //  println(db.renderSql(ordering))

    val game = Game.select
    println(db.run(game))
    val joins = Player.select.join(Game)(_.gameType === _.gameType)
      .filter { case (p, g) => p.xp > 0 }
    println(db.renderSql(joins))
    val joinResult = db.run(joins)
    println(joinResult)
  }
}
