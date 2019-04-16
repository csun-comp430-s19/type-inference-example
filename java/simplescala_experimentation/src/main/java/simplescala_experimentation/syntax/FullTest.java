package simplescala_experimentation.syntax;

import simplescala_experimentation.util.Option;

public class FullTest {
    public final String testName;
    public final Program program;
    public final Option<Type> expectedType;

    public FullTest(final String testName,
                    final Program program,
                    final Option<Type> expectedType) {
        this.testName = testName;
        this.program = program;
        this.expectedType = expectedType;
    }

    public boolean equals(final Object other) {
        if (other instanceof FullTest) {
            final FullTest otherTest = (FullTest)other;
            return (otherTest.testName.equals(testName) &&
                    otherTest.program.equals(program) &&
                    otherTest.expectedType.equals(expectedType));
        } else {
            return false;
        }
    }
} // FullTest

        
