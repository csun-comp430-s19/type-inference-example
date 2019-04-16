package simplescala_experimentation.parser;

import simplescala_experimentation.util.ImmutableList;
import simplescala_experimentation.util.Cons;
import simplescala_experimentation.util.Nil;
import simplescala_experimentation.util.Pair;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

public class SExpParser {
    private final String input;
    private final int len;
    
    public SExpParser(final String input) {
        this.input = input;
        len = input.length();
    }

    public int skipOverWhitespace(int pos) {
        while (pos < len && Character.isWhitespace(input.charAt(pos))) {
            pos++;
        }
        return pos;
    } // skipOverWhitespace

    public Pair<String, Integer> parseQuote(final int startPos) throws SExpParseException {
        if (startPos < len && input.charAt(startPos) == '"') {
            int pos = startPos + 1;
            ImmutableList<Character> accum = new Cons<Character>(new Character('"'),
                                                                 new Nil<Character>());
            boolean escaped = false;

            while (pos < len) {
                final char curChar = input.charAt(pos);
                pos++;
                switch (curChar) {
                case '\\':
                    if (escaped) {
                        accum = new Cons<Character>(new Character('\\'), accum);
                        escaped = false;
                    } else {
                        escaped = true;
                    }
                    break;
                case '"':
                    accum = new Cons<Character>(new Character('"'), accum);
                    if (escaped) {
                        escaped = false;
                    } else {
                        return new Pair<String, Integer>(accum.reverse().mkString(),
                                                         new Integer(pos));
                    }
                    break;
                case 'n':
                    if (escaped) {
                        accum = new Cons<Character>(new Character('\n'), accum);
                        escaped = false;
                    } else {
                        accum = new Cons<Character>(new Character('n'), accum);
                    }
                    break;
                default:
                    if (!escaped) {
                        accum = new Cons<Character>(new Character(curChar), accum);
                    } else {
                        throw new SExpParseException("Escaped non-special character: " + curChar);
                    }
                }
            } // while
            
            throw new SExpParseException("No terminating quote");
        } else {
            throw new SExpParseException("No starting quote");
        }
    } // parseQuote

    private class ParseAtomAccum {
        private ImmutableList<Character> accum;
        public ParseAtomAccum() {
            accum = new Nil<Character>();
        }

        public void addChar(final char c) {
            accum = new Cons<Character>(new Character(c), accum);
        }

        public Pair<SExpAtom, Integer> toResult(final int pos) throws SExpParseException {
            if (accum.isEmpty()) {
                throw new SExpParseException("No characters at position: " + pos);
            } else {
                return new Pair<SExpAtom, Integer>(new SExpAtom(accum.reverse().mkString()),
                                                   new Integer(pos));
            }
        }
    } // ParseAtomAccum
    
    public Pair<SExpAtom, Integer> parseAtom(final int startPos) throws SExpParseException {
        try {
            final Pair<String, Integer> pair = parseQuote(startPos);
            return new Pair<SExpAtom, Integer>(new SExpAtom(pair.first),
                                               pair.second);
        } catch (final SExpParseException e) {
            int pos = startPos;
            final ParseAtomAccum accum = new ParseAtomAccum();
            while (pos < len) {
                final char curChar = input.charAt(pos);
                switch (curChar) {
                case '(':
                case ')':
                    return accum.toResult(pos);
                default:
                    if (Character.isWhitespace(curChar)) {
                        return accum.toResult(pos);
                    } else {
                        accum.addChar(curChar);
                        pos++;
                    }
                }
            }

            return accum.toResult(pos);
        }
    } // parseAtom

    public Pair<SExpList, Integer> parseList(final int startPos) throws SExpParseException {
        if (startPos < len && input.charAt(startPos) == '(') {
            int pos = startPos + 1;
            ImmutableList<SExp> accum = new Nil<SExp>();

            while (pos < len) {
                final char curChar = input.charAt(pos);
                switch (curChar) {
                case '(':
                    final Pair<SExpList, Integer> nestedList = parseList(pos);
                    accum = new Cons<SExp>(nestedList.first, accum);
                    pos = nestedList.second.intValue();
                    break;
                case ')':
                    return new Pair<SExpList, Integer>(new SExpList(accum.reverse()),
                                                       new Integer(pos + 1));
                default:
                    if (Character.isWhitespace(curChar)) {
                        pos++;
                    } else {
                        final Pair<SExpAtom, Integer> nestedAtom = parseAtom(pos);
                        accum = new Cons<SExp>(nestedAtom.first, accum);
                        pos = nestedAtom.second.intValue();
                    }
                }
            }

            throw new SExpParseException("No terminating paren");
        } else {
            throw new SExpParseException("No starting paren");
        }
    } // parseList

    public Pair<SExp, Integer> parseSExp(final int startPos) throws SExpParseException {
        try {
            final Pair<SExpList, Integer> listPair = parseList(startPos);
            return new Pair<SExp, Integer>(listPair.first, listPair.second);
        } catch (final SExpParseException e) {
            final Pair<SExpAtom, Integer> atomPair = parseAtom(startPos);
            return new Pair<SExp, Integer>(atomPair.first, atomPair.second);
        }
    } // parseSExp

    public ImmutableList<SExp> parse() throws SExpParseException {
        ImmutableList<SExp> accum = new Nil<SExp>();
        int pos = skipOverWhitespace(0);
        while (pos < len) {
            final Pair<SExp, Integer> pair = parseSExp(pos);
            accum = new Cons<SExp>(pair.first, accum);
            pos = skipOverWhitespace(pair.second.intValue());
        }

        return accum.reverse();
    } // parse

    public static ImmutableList<SExp> parse(final String input) throws SExpParseException {
        return new SExpParser(input).parse();
    }

    public static ImmutableList<String> readLinesFromReaderNoClose(final BufferedReader reader) throws IOException {
        ImmutableList<String> retval = new Nil<String>();
        String line = null;
        while ((line = reader.readLine()) != null) {
            retval = new Cons<String>(line, retval);
        }
        return retval.reverse();
    }

    // closes file at the end
    public static ImmutableList<String> readLinesFromReader(final BufferedReader reader) throws IOException {
        final ImmutableList<String> retval = readLinesFromReaderNoClose(reader);
        reader.close();
        return retval;
    }

    public static ImmutableList<SExp> parse(final File file) throws SExpParseException, IOException {
        return parse(readLinesFromReader(new BufferedReader(new FileReader(file))).mkString("\n"));
    }
}

