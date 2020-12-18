import edu.joshuacrotts.littlec.exec.ParserTest;
import edu.joshuacrotts.littlec.icode.ICode;
import edu.joshuacrotts.littlec.main.LCErrorListener;
import edu.joshuacrotts.littlec.main.LCListener;
import edu.joshuacrotts.littlec.syntaxtree.LCSyntaxTree;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Testing file for the parser. This tester is designed to run test cases
 * that are located in the "tests" subdirectory of the main project directory.
 * Valid LittleC programs (which should parse and produce a syntax tree) are
 * tested by method goodFileTest(), and invalid LittleC program (which should
 * produce null for the syntax tree, indicating an error) are tested by method
 * badFileInput(). See those methods for more information.
 */
public class ParserTester {
  
    /**
     * Helper function to count number of newlines in a string
     * @param s the string
     * @return the number of newlines
     */
    private static int countNLs(String s) {
        if (s == null) return 0;
        int count = 0;
        for (int i=0; i<s.length(); i++) {
            if (s.charAt(i) == '\n')
                count++;
        }
        return count;
    }

    /**
     * Compares to byte array token by token, where a "token" is either a
     * C-style identifier, a number, or an individual character. All whitespace
     * is skipped over and not used for the comparison, so the outputs can
     * be formatted/spaced entirely differently.
     *
     * @param got the bytes printed out by the program under test
     * @param expect the expected output
     */
    private static void compare(byte[] got, byte[] expect) {
        String result = null;
        Scanner gotScanner = new Scanner(new ByteArrayInputStream(got));
        Scanner expScanner = new Scanner(new ByteArrayInputStream(expect));
        expScanner.useDelimiter("\\n");
        int gotLine = 1;
        int expLine = 1;

        Pattern tokPattern = Pattern.compile("([A-Za-z_][A-Za-z_0-9]*)|([0-9]+)|(.)");
        Pattern skipPattern = Pattern.compile("[ \\r\\t\\n]*");
        Pattern nlPattern = Pattern.compile("\\n");

        boolean done = false;
        while (!done) {
            String skipped = expScanner.findWithinHorizon(skipPattern, 1000);
            expLine += countNLs(skipped);
            String expToken = expScanner.findWithinHorizon(tokPattern, 1000);

            skipped = gotScanner.findWithinHorizon(skipPattern, 1000);
            gotLine += countNLs(skipped);
            String gotToken = gotScanner.findWithinHorizon(tokPattern, 1000);
            if (expToken != null) {
                if (gotToken != null) {
                    if (!expToken.equals(gotToken)) {
                        result = "Error. Got line " + gotLine + " has \"" + gotToken
                                + "\"; expected line " + expLine + " is \"" + expToken + "\"";
                        done = true;
                    }
                } else {
                    result = "Produced output ended too early - expected \""
                            +expToken+"\" (line "+expLine+")";
                    done = true;
                }
            } else {
                if (gotToken != null) {
                    result = "Got extra output: unexpected \""+gotToken
                            +"\" (line "+gotLine+")";
                }
                done = true;
            }
        }

        assertNull(result, result);
    }

    /**
     * The testing engine for a valid LittleC program (which should parse and
     * produce a LCSyntaxTree). Both the input LittleC program and the expected
     * syntax tree output file must be provided as files with ".in" and ".out"
     * extensions, respectively. Runs input file through the
     * ParserTest.parseFromFile() method, gets the syntax tree and calls the
     * user-written printSyntaxTree() method to get a text representation,
     * which is matched token-by-token with the expected output.
     *
     * @param testName the base name of the test case; files are stored in the
     *                 tests project directory, with ".in" and ".out"
     *                 extensions.
     */
    private static void goodFileTest(String testName) {
        String inName = "tests/" + testName + ".in";
        String expName = "tests/" + testName + ".out";

        PrintStream origOut = System.out;
        PrintStream origErr = System.err;
        ByteArrayOutputStream captureOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captureOut));
        System.setErr(new PrintStream(captureOut));
        LCListener parser = ParserTest.parseFromFile(inName);
        if (parser == null)
            throw new AssertionFailedError("Failed reading test input file "+inName);
        LCSyntaxTree syntaxTree = parser.getSyntaxTree();
        syntaxTree.printSyntaxTree();
        System.setErr(origErr);
        System.setOut(origOut);
        byte[] actual = captureOut.toByteArray();

        byte[] expected;
        try {
            expected = Files.readAllBytes(Paths.get(expName));
        } catch (IOException e) {
            throw new AssertionFailedError("Missing expected output file " + expName);
        }
        compare(actual, expected);
        ParserTester.cleanup();
    }

    /**
     * The testing engine for a invalid LittleC program (the error should be
     * detected, resulting in null being returned for the syntax tree. Any
     * non-null result means the error was missed, so the test fails. Since
     * there is not supposed to be any output, only the input file (with
     * extension ".in") is required.
     *
     * @param testName the base name of the test case; files are stored in the
     *                 tests project directory, with an ".in" extensions.
     */
    private static void errorFileTest(String testName) {
        String inName = "tests/" + testName + ".in";

        PrintStream origOut = System.out;
        PrintStream origErr = System.err;
        ByteArrayOutputStream captureOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captureOut));
        System.setErr(new PrintStream(captureOut));
        LCListener parser = ParserTest.parseFromFile(inName);
        if (parser == null)
            return;

        LCSyntaxTree result = parser.getSyntaxTree();
        System.setErr(origErr);
        System.setOut(origOut);
        if (result != null)
            throw new AssertionFailedError("Did not catch the error in input "+inName);
        
        ParserTester.cleanup();
    }
    
    /**
     * Cleanup function. I originally used this with the @AfterEach tag, but because
     * there are group tests that rely on a cleanup, I had to force this into a
     * function and call it before the assertion in runICTest(...).
     * 
     * Removes the errors.
     */
    private static void cleanup() {
      LCErrorListener.reset();
    }

    /**
     * Instructor-provided test.  Very basic valid-program tests.
     */
    @Test
    public void doGoodTestA() {
        goodFileTest("good1a");
    }

    /**
     * Instructor-provided test.  Very basic valid-program tests.
     */
    @Test
    public void doGoodTestB() {
        goodFileTest("good1b");
    }

    /**
     * Instructor-provided test.  A little harder - using a for loop.
     */
    @Test
    public void doGoodTestC() {
        goodFileTest("good1c");
    }

    /**
     * Instructor-provided test.  A little harder - forward fn declaration.
     */
    @Test
    public void doGoodTestD() {
        goodFileTest("good1d");
    }

    /**
     * Instructor-provided test.  A little harder - recursive function
     */
    @Test
    public void doGoodTestE() {
        goodFileTest("good1e");
    }

    /**
     * Instructor-provided test.
     */
    @Test
    public void doGoodTest2A() {
        goodFileTest("good2a");
    }

    /**
     * Instructor-provided test.
     */
    @Test
    public void doGoodTest2B() {
        goodFileTest("good2b");
    }

    /**
     * Instructor-provided test.
     */
    @Test
    public void doGoodTest2C() {
        goodFileTest("good2c");
    }

    /**
     * Instructor-provided test.
     */
    @Test
    public void doGoodTest2D() {
        goodFileTest("good2d");
    }

    /**
     * Instructor-provided test.
     */
    @Test
    public void doGoodTest2E() {
        goodFileTest("good2e");
    }

    /**
     * Instructor-provided test.
     */
    @Test
    public void doGoodTest2F() {
        goodFileTest("good2f");
    }

    /**
     * Instructor-provided test. Error should cause parsing fail.
     */
    @Test
    public void doErrTestA() {
        errorFileTest("bad1a");
    }

    /**
     * Instructor-provided test.  Error should cause parsing fail.
     */
    @Test
    public void doErrTestB() {
        errorFileTest("bad1b");
    }

    /**
     * Instructor-provided test.  Error should cause parsing fail.
     */
    @Test
    public void doErrTestC() {
        errorFileTest("bad1c");
    }

    /**
     * Instructor-provided test.  Error should cause parsing fail.
     */
    @Test
    public void doErrTestD() {
        errorFileTest("bad1d");
    }

    /**
     * Instructor-provided test.  Error should cause parsing fail.
     */
    @Test
    public void doErrTestE() {
        errorFileTest("bad1e");
    }

    /**
     * Instructor-provided test.  Error should cause parsing fail.
     */
    @Test
    public void doErrTestF() {
        errorFileTest("bad1f");
    }

    /**
     * Instructor-provided test.  Error should cause parsing fail.
     */
    @Test
    public void doErrTestG() {
        errorFileTest("bad1g");
    }

    /**
     * Instructor-provided test.  Error should cause parsing fail.
     */
    @Test
    public void doErrTestH() {
        errorFileTest("bad1h");
    }

    /**
     * Instructor-provided test. Error should cause parsing fail.
     */
    @Test
    public void doErrTestI() {
        errorFileTest("bad1i");
    }

    /**
     * Instructor-provided test.  Error should cause parsing fail.
     */
    @Test
    public void doErrTestJ() {
        errorFileTest("bad1j");
    }

    /**
     * Instructor-provided test.  Error should cause parsing fail.
     */
    @Test
    public void doErrTestK() {
        errorFileTest("bad1k");
    }

    /**
     * Instructor-provided test.  Error should cause parsing fail.
     */
    @Test
    public void doErrTest2A() {
        errorFileTest("bad2a");
    }

    /**
     * Instructor-provided test.  Error should cause parsing fail.
     */
    @Test
    public void doErrTest2B() {
        errorFileTest("bad2b");
    }

    /**
     * Instructor-provided test.  Error should cause parsing fail.
     */
    @Test
    public void doErrTest2C() {
        errorFileTest("bad2c");
    }

    /**
     * Instructor-provided test.  Error should cause parsing fail.
     */
    @Test
    public void doErrTest2D() {
        errorFileTest("bad2d");
    }

    /**
     * Instructor-provided test.  Error should cause parsing fail.
     */
    @Test
    public void doErrTest2E() {
        errorFileTest("bad2e");
    }

    /**
     * Instructor-provided test.  Error should cause parsing fail.
     */
    @Test
    public void doErrTest2F() {
        errorFileTest("bad2f");
    }

    /**
     * Instructor-provided test.  Error should cause parsing fail.
     */
    @Test
    public void doErrTest2G() {
        errorFileTest("bad2g");
    }

    /**
     * Instructor-provided test.  Error should cause parsing fail.
     */
    @Test
    public void doErrTest2H() {
        errorFileTest("bad2h");
    }

    /**
     * Student-provided test. You make as many methods as you want for
     * different tests, as long as you don't use the "doTestX" naming.
     */
    @Test
    public void doUserTest() {
        // Can put test code here.
    }
}
