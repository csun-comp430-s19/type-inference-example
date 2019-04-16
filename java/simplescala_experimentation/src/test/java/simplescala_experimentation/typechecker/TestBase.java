package simplescala_experimentation.typechecker;

import simplescala_experimentation.util.Option;
import simplescala_experimentation.syntax.FullTest;
import simplescala_experimentation.syntax.Type;
import simplescala_experimentation.parser.SimpleScalaSExpParser;
import simplescala_experimentation.typechecker.Typechecker;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public class TestBase {
    public static final int NUM_TESTS = 1000;
    
    @Parameterized.Parameters(name= "{0}")
    public static Iterable<ZipfileReadEntry> entries() {
        final File zipfile = new File("../../scala/test_programs/generated_well_typed.zip");
	// final File zipfile = new File("../../scala/test_programs/generated_ill_typed.zip");
	// final File zipfile = new File("../../scala/test_programs/generated_almost_well_typed.zip");
        // final File zipfile = new File("test_programs/basic_tests.zip");
	try {
	    return new TakeIterable(NUM_TESTS, new ZipfileReader(zipfile));
	} catch (final IOException e) {
	    throw new RuntimeException(e.getMessage());
	}
    }

    private final FullTest test;
    public TestBase(final ZipfileReadEntry entry) {
	test = parseTest(entry);
    }

    @Test
    public void test() {
	runTest(test);
    }
    
    public static FullTest parseTest(final ZipfileReadEntry entry) {
        return SimpleScalaSExpParser.parseTestFromString(entry.filename,
                                                         entry.contents.mkString("\n"));
    }

    // public static void fail(final String str) {
    //     System.out.println(str);
    // }
    
    public static void runTest(final FullTest test) {
        final Option<Type> expected = test.expectedType;
        final Option<Type> received = Typechecker.opProgramType(test.program);
        if (expected.isDefined()) {
            final Type expect = expected.get();
            if (received.isDefined()) {
                if (!Typechecker.typesIsomorphic(expect, received.get())) {
                    fail(test.testName +
                         "\nExpected type: " + expect.toString() + "\n" +
                         "Received type: " + received.get().toString());
                }
            } else {
                fail(test.testName +
                     "\nExpected type: " + expect + "\n" +
                     "Typechecker returned ill-typed");
            }
        } else {
            if (received.isDefined()) {
                fail(test.testName +
                     "\nExpected ill-typed\nTypechecker returned: " +
                     received.get().toString());
            }
        }
    }
}

                
