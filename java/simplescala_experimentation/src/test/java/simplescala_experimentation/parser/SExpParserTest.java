package simplescala_experimentation.parser;

import simplescala_experimentation.util.Pair;
import simplescala_experimentation.util.ImmutableList;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class SExpParserTest {
    @Test
    public void testParseQuote_empty_string() {
        final SExpParser parser = new SExpParser("\"\"");
        final Pair<String, Integer> pair = parser.parseQuote(0);
        assertEquals("\"\"", pair.first);
        assertEquals(2, pair.second.intValue());
    }

    @Test
    public void testParseQuote_nonempty_string() {
        final SExpParser parser = new SExpParser("\"foo\"");
        final Pair<String, Integer> pair = parser.parseQuote(0);
        assertEquals("\"foo\"", pair.first);
        assertEquals(5, pair.second.intValue());
    }

    @Test
    public void testParseAtom_foo() {
        final SExpParser parser = new SExpParser("foo");
        final Pair<SExpAtom, Integer> pair = parser.parseAtom(0);
        assertEquals(new SExpAtom("foo"), pair.first);
        assertEquals(3, pair.second.intValue());
    }

    @Test
    public void testParseList_1() {
        final SExpParser parser = new SExpParser("(foo)");
        final Pair<SExpList, Integer> pair = parser.parseList(0);
        assertEquals(makeList(new SExpAtom("foo")), pair.first);
        assertEquals(5, pair.second.intValue());
    }

    @Test
    public void testParseList_2() {
        final SExpParser parser = new SExpParser("(foo)bar");
        final Pair<SExpList, Integer> pair = parser.parseList(0);
        assertEquals(makeList(new SExpAtom("foo")), pair.first);
        assertEquals(5, pair.second.intValue());
    }

    @Test
    public void testParseList_3() {
        final SExpParser parser = new SExpParser("(foo bar)");
        final Pair<SExpList, Integer> pair = parser.parseList(0);
        assertEquals(makeList(new SExpAtom("foo"), new SExpAtom("bar")), pair.first);
        assertEquals(9, pair.second.intValue());
    }

    @Test
    public void testParseList_4() {
        final SExpParser parser = new SExpParser("(foo    bar)");
        final Pair<SExpList, Integer> pair = parser.parseList(0);
        assertEquals(makeList(new SExpAtom("foo"), new SExpAtom("bar")), pair.first);
        assertEquals(12, pair.second.intValue());
    }

    @Test
    public void testParseList_5() {
        final SExpParser parser = new SExpParser("(foo    bar  )");
        final Pair<SExpList, Integer> pair = parser.parseList(0);
        assertEquals(makeList(new SExpAtom("foo"), new SExpAtom("bar")), pair.first);
        assertEquals(14, pair.second.intValue());
    }
    
    public static SExp makeList(final SExp... sexps) {
        return new SExpList(ImmutableList.makeList(sexps));
    }
}
