package librarydemos

import upickle.default.*

object UPickleDemo {
  def main(args: Array[String]): Unit = {
    val jsonNumber = write(42)
    val jsonString = write("Scala")
    println(s"$jsonNumber $jsonString")

    val readN = read[Int](jsonNumber)

    case class Person(name: String, age: Int) derives ReadWriter

    val person = Person("Alice", 25)
    val personJson = write(person)
    println(personJson)

    val personAgain = read[Person](personJson)
    println(s"is same person?: ${person == personAgain}")

    case class Address(street: String, city: String) derives ReadWriter
    case class Employee(name: String, age: Int, address: List[Address]) derives ReadWriter

    val employee = Employee("Bob", 30, List(Address("123 Main St", "New York"), Address("456 Maple Ave", "Boston")))
    val employeeJson = write(employee)
    println(employeeJson)

    // custom field names
    // User(user_id, user_name)
    case class User(
                     @upickle.implicits.key("user_id") id: Int,
                     @upickle.implicits.key("user_name") name: String
                   ) derives ReadWriter
    val user = User(1, "Charlie")
    val userJson = write(user)
    println(userJson)

    sealed trait Pet derives ReadWriter // automatically derive ReadWriter for sealed traits
    case class Dog(name: String, breed: String) extends Pet
    case class Cat(name: String, lives: Int) extends Pet

    val pets: List[Pet] = List(Dog("Buddy", "Golden Retriever"), Cat("Whiskers", 9))
    val petsJson = write(pets)
    println(petsJson)

    val complexJson =
      """
        |{
        |  "name": "Eve",
        |  "age": 28,
        |  "address": {"city": "San Francisco", "street": "789 Pine St"},
        |  "hobbies": ["reading", "hiking", "coding"]
        |}
        |""".stripMargin


    val jsonAst = read[ujson.Value](complexJson)
    val name = jsonAst("name").str
    val age = jsonAst("age").num
    val city = jsonAst("address")("city").str
    val hobbies = jsonAst("hobbies").arr.map(_.str).toList
    println(s"$name, $age, $city, ${hobbies.mkString(", ")}")
  }
}
