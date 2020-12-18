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
import edu.joshuacrotts.littlec.main.LCErrorListener;

/**
 * Testing file for intermediate code generation (phase 4 of the
 * compiler project). This tester is designed to run test cases that are located
 * in the "tests" subdirectory of the main project directory. All test programs
 * are valid LittleC programs whose output varies depending on some
 * feature/requirement of LittleC. Output must match exactly with the provided
 * output file.
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
  private static void cleanup() {
    ICode.cleanup();
    LCErrorListener.reset();
  }

  @Test
  public void icTestA() {
    runICTest("test3a");
  }

  @Test
  public void icTestB() {
    runICTest("test3b");
  }

  @Test
  public void icTestC() {
    runICTest("test3c");
  }

  @Test
  public void icTestD() {
    runICTest("test3d");
  }

  @Test
  public void icTestE() {
    runICTest("test3e");
  }

  @Test
  public void icTestF() {
    runICTest("test3f");
  }

  @Test
  public void icTestG() {
    runICTest("test3g");
  }

  @Test
  public void icTestH() {
    runICTest("test3h");
  }

  @Test
  public void icTestI() {
    runICTest("test3i");
  }

  @Test
  public void icTestJ() {
    runICTest("test3j");
  }

  @Test
  public void icTestK() {
    runICTest("test3k");
  }

  @Test
  public void icTestL() {
    runICTest("test3l");
  }

  @Test
  public void icTestM() {
    runICTest("test3m");
  }

  @Test
  public void icTestN() {
    runICTest("test3n");
  }

  @Test
  public void icTestO() {
    runICTest("test3o");
  }
  
  @Test
  public void icTestP() {
    runICTest("test3p");
  }
  
  @Test
  public void icTestQ() {
    runICTest("test3q");
  }
  
  @Test
  public void icTestR() {
    runICTest("test3r");
  }
  
  @Test
  public void icTestS() {
    runICTest("test3s");
  }
  
  @Test
  public void icTestT() {
    runICTest("test3t");
  }
  
  @Test
  public void icTestU() {
    runICTest("test3u");
  }

  @Test
  public void icTestV() {
    runICTest("test3v");
  }
  
  @Test
  public void icTestW() {
    runICTest("test3w");
  }
  
  @Test
  public void icTestX() {
    runICTest("test3x");
  }
  
  @Test
  public void icTestY() {
    runICTest("test3y");
  }
  
  @Test
  public void myTest1() {
    runICTest("test_joshuacrotts_1");
  }
  
  @Test
  public void myTest2() {
    runICTest("test_joshuacrotts_2");
  }
  
  @Test
  public void myTest3() {
    runICTest("test_joshuacrotts_3");
  }
  
  @Test
  public void myTest4() {
    runICTest("test_joshuacrotts_4");
  }
  
  @Test
  public void myTest5() {
    runICTest("test_joshuacrotts_5");
  }
  
  @Test
  public void myTest6() {
    runICTest("test_joshuacrotts_6");
  }
  
  @Test
  public void myTest7() {
    runICTest("test_joshuacrotts_7");
  }
  
  @Test
  public void myTest8() {
    runICTest("test_joshuacrotts_8");
  }
}