package librarydemos

import os.*

object OSLibDemo {

  def main(args: Array[String]): Unit = {
    println("current directory" + os.pwd)
    println("home directory" + os.home)

    val tempDir= os.temp.dir(prefix = "os-lib-demo-")
    println("created temp directory: "+ tempDir)

    val testFile = tempDir / "test.txt"
    os.write(testFile,"Hello from OS-lib")
    println("written to file " + testFile)

    val content = os.read(testFile)
    println("file content: "+ content)

    os.remove.all(tempDir)
  }
}
