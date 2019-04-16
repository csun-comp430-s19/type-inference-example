package simplescala_experimentation.polytyped

sealed trait SExp {
  def asString(): String
}
case class SExpAtom(atom: String) extends SExp {
  def asString(): String = atom
}
case class SExpList(list: List[SExp]) extends SExp {
  def asString(): String = {
    "(" + list.map(_.asString).mkString(" ") + ")"
  }
}

object SExpParser {
  type ParseResult[A] = (A, Int)
}

// the parser is intentionally not built on parser combinators
// for performance reasons
class SExpParser(private val input: String) {
  import SExpParser.ParseResult

  // ---BEGIN CONSTRUCTOR---
  private val len = input.length
  // ---END CONSTRUCTOR---

  // the quotes will end up in the result string
  def parseQuote(startPos: Int): Option[ParseResult[String]] = {
    @scala.annotation.tailrec
    def parseFrom(startPos: Int, accum: List[Char], escaped: Boolean): Option[ParseResult[String]] = {
      if (startPos < len) {
        val nextPos = startPos + 1
        input.charAt(startPos) match {
          case '\\' => {
            if (escaped) {
              parseFrom(nextPos, '\\' :: accum, false)
            } else {
              parseFrom(nextPos, accum, true)
            }
          }
          case '"' => {
            val newAccum = '"' :: accum
            if (escaped) {
              parseFrom(nextPos, newAccum, false)
            } else {
              Some((newAccum.reverse.mkString -> nextPos))
            }
          }
          case 'n' if escaped => {
            parseFrom(nextPos, '\n' :: accum, false)
          }
          case o => {
            if (!escaped) {
              parseFrom(nextPos, o :: accum, false)
            } else {
              // escaped some other character
              None
            }
          }
        }
      } else {
        // no terminating quote
        None
      }
    }

    if (startPos < len && input.charAt(startPos) == '"') {
      parseFrom(startPos + 1, List('"'), false)
    } else {
      None
    }
  }

  def parseAtom(startPos: Int): Option[ParseResult[SExpAtom]] = {
    @scala.annotation.tailrec
    def parseFrom(startPos: Int, accum: List[Char]): Option[ParseResult[SExpAtom]] = {
      def done(): Option[ParseResult[SExpAtom]] = {
	if (accum.isEmpty) {
	  None
	} else {
	  Some((SExpAtom(accum.reverse.mkString), startPos))
	}
      }

      if (startPos < len) {
	input.charAt(startPos) match {
	  case '(' | ')' => done
	  case o => {
	    if (Character.isWhitespace(o)) {
	      done
	    } else {
	      parseFrom(startPos + 1, o :: accum)
	    }
	  }
	}
      } else {
	done
      }
    }

    parseQuote(startPos) match {
      case Some((quote, nextPos)) => Some((SExpAtom(quote) -> nextPos))
      case None => parseFrom(startPos, List())
    }
  }

  def parseList(startPos: Int): Option[ParseResult[SExpList]] = {
    @scala.annotation.tailrec
    def parseFrom(startPos: Int, accum: List[SExp]): Option[ParseResult[SExpList]] = {
      if (startPos < len) {
	input.charAt(startPos) match {
	  case '(' => {
	    parseList(startPos) match {
	      case Some((sexp, newPos)) => {
		parseFrom(newPos, sexp :: accum)
	      }
	      case None => None
	    }
	  }
	  case ')' => {
	    Some((SExpList(accum.reverse), startPos + 1))
	  }
	  case o => {
	    if (Character.isWhitespace(o)) {
	      parseFrom(startPos + 1, accum)
	    } else {
	      parseAtom(startPos) match {
		case Some((atom, newPos)) => {
		  parseFrom(newPos, atom :: accum)
		}
		case None => None
	      }
	    }
	  }
	}
      } else {
	None
      }
    }

    if (startPos < len && input.charAt(startPos) == '(') {
      parseFrom(startPos + 1, List())
    } else {
      None
    }
  }

  // returns the first position that doesn't start with whitespace, which
  // might be beyond the start of the string
  @scala.annotation.tailrec
  final def skipOverWhitespace(startPos: Int): Int = {
    if (startPos < len && Character.isWhitespace(input.charAt(startPos))) {
      skipOverWhitespace(startPos + 1)
    } else {
      startPos
    }
  }

  def parseSExp(startPos: Int): Option[ParseResult[SExp]] = {
    parseList(startPos) match {
      case s@Some(_) => s
      case None => parseAtom(startPos)
    }
  }

  def parse(): Option[List[SExp]] = {
    @scala.annotation.tailrec
    def parseFrom(startPos: Int, accum: List[SExp]): Option[List[SExp]] = {
      val first = skipOverWhitespace(startPos)
      if (first < len) {
	parseSExp(first) match {
	  case Some((sexp, newPos)) => {
	    parseFrom(newPos, sexp :: accum)
	  }
	  case None => None
	}
      } else {
	Some(accum.reverse)
      }
    }

    parseFrom(0, List())
  }
}
