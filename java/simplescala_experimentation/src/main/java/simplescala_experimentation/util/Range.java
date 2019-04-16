package simplescala_experimentation.util;

public class Range {
    public static ImmutableList<Integer> until(final int begin, final int end) {
        if (begin >= end) {
            return new Nil<Integer>();
        } else {
            return new Cons<Integer>(new Integer(begin),
                                     until(begin + 1, end));
        }
    }

    public static ImmutableList<Integer> to(final int begin, final int end) {
        return until(begin, end + 1);
    }
}
