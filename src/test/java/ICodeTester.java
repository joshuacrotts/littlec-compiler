import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import edu.joshuacrotts.littlec.exec.RunCode;
import edu.joshuacrotts.littlec.icode.ICode;

/**
 * Testing file for CSC 439 intermediate code generation (phase 4 of the
 * compiler project). This tester is designed to run test cases that are located
 * in the "tests" subdirectory of the main project directory. All test programs
 * are valid LittleC programs whose output varies depending on some
 * feature/requirement of LittleC. Output must match exactly with the provided
 * output file.
 * <p>
 * Instructor-provided test cases will be handled with methods named
 * phase4TestA(), phase4TestB(), etc. If you want to provide your own tests, put
 * them in methods named differently, such as the "doUserTests()" method below.
 * These will not be run from the GitHub classroom autotester, but can be run as
 * part of your development/testing.
 */

public class ICodeTester {
  /**
   * The testing engine. Compiles and runs input file using the RunCode class,
   * capturing standard output to compare with the expected output. Non-matching
   * (failed) tests could provide more useful output, but for now this will have
   * to do.
   *
   * @param testName the base name of the test case; files are stored in the tests
   *                 project directory, with ".in" and ".out" extensions.
   */
  private static void runICTest(String testName) {
    try {
      String inName = "tests/" + testName + ".in";
      String expName = "tests/" + testName + ".out";

      PrintStream origOut = System.out;
      PrintStream origErr = System.err;
      ByteArrayOutputStream captureOut = new ByteArrayOutputStream();
      System.setOut(new PrintStream(captureOut));
      System.setErr(new PrintStream(captureOut));
      String[] args = new String[1];
      args[0] = inName;
      RunCode.main(args);
      System.setErr(origErr);
      System.setOut(origOut);
      String[] actual = captureOut.toString().split("\\r?\\n");
      String[] expected = Files.readAllLines(Paths.get(expName)).toArray(new String[0]);

      // System.out.println(java.util.Arrays.toString(actual));
      // System.out.println(java.util.Arrays.toString(expected));

      ICodeTester.cleanup();
      assertArrayEquals(expected, actual, "Files differ");
    } catch (IOException e) {
      throw new AssertionFailedError("Missing test case: " + testName);
    }
  }

  /**
   * Cleanup function. I originally used this with the @AfterEach tag, but because
   * there are group tests that rely on a cleanup, I had to force this into a
   * function and call it before the assertion in runICTest(...).
   * 
   * Removes the global [static] stack storage.
   */
  public static void cleanup() {
    ICode.cleanup();
  }

  /**
   * Instructor-provided test - Stage 4.
   */
  @Test
  public void phase4TestA() {
    runICTest("test3a");
  }

  /**
   * Instructor-provided test - Stage 4.
   */
  @Test
  public void phase4TestB() {
    runICTest("test3b");
  }

  /**
   * Instructor-provided test - Stage 4.
   */
  @Test
  public void phase4TestC() {
    runICTest("test3c");
  }

  /**
   * Instructor-provided test - Stage 4.
   */
  @Test
  public void phase4TestD() {
    runICTest("test3d");
  }

  /**
   * Instructor-provided test - Stage 4.
   */
  @Test
  public void phase4TestE() {
    runICTest("test3e");
  }

  /**
   * Instructor-provided test - Stage 4.
   */
  @Test
  public void phase4TestF() {
    runICTest("test3f");
  }

  /**
   * Instructor-provided test - Stage 4.
   */
  @Test
  public void phase4TestG() {
    runICTest("test3g");
  }

  /**
   * Instructor-provided test - Stage 4.
   */
  @Test
  public void phase4TestH() {
    runICTest("test3h");
  }

  /**
   * Instructor-provided test - Stage 4.
   */
  @Test
  public void phase4TestI() {
    runICTest("test3i");
  }

  /**
   * Instructor-provided test - Stage 4.
   */
  @Test
  public void phase4TestJ() {
    runICTest("test3j");
  }

  /**
   * Instructor-provided test - Stage 4.
   */
  @Test
  public void phase4TestK() {
    runICTest("test3k");
  }

  /**
   * Instructor-provided test - Stage 4.
   */
  @Test
  public void phase4TestL() {
    runICTest("test3l");
  }

  /**
   * Instructor-provided test - Stage 4.
   */
  @Test
  public void phase4TestM() {
    runICTest("test3m");
  }

  /**
   * Batch tests so that it's more efficient on a GitHub push
   */
  @Test
  public void phase4Batch1() {
    runICTest("test3a");
    runICTest("test3b");
    runICTest("test3c");
    runICTest("test3d");
    runICTest("test3e");
  }

  /**
   * Batch tests so that it's more efficient on a GitHub push
   */
  @Test
  public void phase4Batch2() {
    runICTest("test3f");
    runICTest("test3g");
    runICTest("test3h");
    runICTest("test3i");
    runICTest("test3j");
  }

  /**
   * Batch tests so that it's more efficient on a GitHub push
   */
  @Test
  public void phase4Batch3() {
    runICTest("test3k");
  }

  /**
   * Student-provided test. You make as many methods as you want for different
   * tests, as long as you don't use the "doTestX" naming.
   */
  @Test
  public void doUserTest() {
    runICTest("test_joshuacrotts_2");
    runICTest("test_joshuacrotts_3");
    runICTest("test_joshuacrotts_4");
    runICTest("test_joshuacrotts_5");
    runICTest("test_joshuacrotts_7");
  }
}