package simplescala_experimentation.polytyped

import java.io.File

case class ZipfileReadEntry(filename: String, contents: Seq[String])

class ZipfileReader(zipfile: File) {
  import java.io.{BufferedInputStream,
		  FileInputStream,
		  BufferedReader,
		  InputStreamReader}
  import java.util.zip.ZipInputStream

  // ---BEGIN CONSTRUCTOR---
  private val input =
    new ZipInputStream(
      new BufferedInputStream(
	new FileInputStream(zipfile)))
  // ---END CONSTRUCTOR---

  // not thread safe
  def nextEntry(): Option[ZipfileReadEntry] = {
    val zipEntry = input.getNextEntry()
    if (zipEntry eq null) {
      input.close()
      return None
    }

    val reader =
      new BufferedReader(
	new InputStreamReader(input))

    @scala.annotation.tailrec
    def loop(accum: List[String]): Seq[String] = {
      reader.readLine match {
	case null => {
	  accum.reverse.toSeq
	}
	case s => {
	  loop(s :: accum)
	}
      }
    }

    Some(ZipfileReadEntry(zipEntry.getName, loop(List())))
  }
}
