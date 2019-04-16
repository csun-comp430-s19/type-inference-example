package simplescala_experimentation.parser;

import simplescala_experimentation.util.ImmutableList;
import simplescala_experimentation.util.Pair;
import simplescala_experimentation.util.Predicate;

import java.util.Arrays;

public class ConstantListPattern extends SExpPattern {
    private final ImmutableList<SExpPattern> expect;
    
    public ConstantListPattern(final SExpPattern... expect) {
        this.expect = ImmutableList.makeList(expect);
    }

    public boolean matches(final SExp input) {
        return input.visitSExp(new MatchVisitor() {
                public boolean visit(final ImmutableList<SExp> list) {
                    if (expect.size() == list.size()) {
                        return expect.zip(list).forall(new Predicate<Pair<SExpPattern, SExp>>() {
                                public boolean matches(final Pair<SExpPattern, SExp> pair) {
                                    return pair.first.matches(pair.second);
                                }
                            });
                    } else {
                        return false;
                    }
                }
            });
    }

    public String toString() {
        return "ConstantListPattern(" + expect.mkString(", ") + ")";
    }
}

                            
