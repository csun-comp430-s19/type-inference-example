package simplescala_experimentation.polytyped

import java.io.File

object TestContainer {
  type FileName = String
  type FileContents = String

  def isSimpleScala(name: String): Boolean = {
    name.endsWith(".simplescala") || isSimpleScalaSExp(name)
  }

  def isSimpleScalaSExp(name: String): Boolean = {
    name.endsWith(".simplescala_sexp")
  }
}
import TestContainer._

trait TestContainer {
  protected def nextFile(): Option[(FileName, FileContents)]

  def foreachFile(doThis: (FileName, FileContents) => Unit) {
    var cur = nextFile()
    while (cur.isDefined) {
      val Some((name, contents)) = cur
      if (isSimpleScala(name)) {
        doThis(name, contents)
      }
      cur = nextFile()
    }
  }
}

class DirectoryTestContainer(private val dir: File) extends TestContainer {
  assert(dir.isDirectory)
  private val files = dir.listFiles().iterator

  protected def nextFile(): Option[(FileName, FileContents)] = {
    if (files.hasNext) {
      val file = files.next()
      Some((file.getName -> SimpleScalaParser.fileContents(file)))
    } else {
      None
    }
  }
}

class ZipTestContainer(private val zipfile: File) extends TestContainer {
  private val reader = new ZipfileReader(zipfile)

  protected def nextFile(): Option[(FileName, FileContents)] = {
    reader.nextEntry().map(
      { case ZipfileReadEntry(name, contents) =>
          (name -> contents.mkString("\n")) })
  }
}

