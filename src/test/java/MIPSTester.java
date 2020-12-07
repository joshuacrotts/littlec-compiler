import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import edu.joshuacrotts.littlec.exec.RunMIPS;
import edu.joshuacrotts.littlec.icode.ICode;

/**
 * Testing file for the MIPS generation of the project.
 * 
 * @author Joshua Crotts
 */
public class MIPSTester {
  /**
   * The testing engine. Compiles and runs input file using the RunMIPS class,
   * capturing standard output to compare with the expected output. Non-matching
   * (failed) tests could provide more useful output, but for now this will have
   * to do.
   *
   * @param testName the base name of the test case; files are stored in the tests
   *                 project directory, with ".in" and ".out" extensions.
   */
  private static void runMIPSTest(String testName) {
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
      RunMIPS.main(args);
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
   * function and call it before the assertion in runMIPSTest(...).
   * 
   * Removes the global [static] stack storage.
   */
  public static void cleanup() {
    ICode.cleanup();
  }

  @Test
  public void mipsTestA() {
    runMIPSTest("test3a");
  }

  @Test
  public void mipsTestB() {
    runMIPSTest("test3b");
  }

  @Test
  public void mipsTestC() {
    runMIPSTest("test3c");
  }

  @Test
  public void mipsTestD() {
    runMIPSTest("test3d");
  }

  @Test
  public void mipsTestE() {
    runMIPSTest("test3e");
  }

  @Test
  public void mipsTestF() {
    runMIPSTest("test3f");
  }

  @Test
  public void mipsTestG() {
    runMIPSTest("test3g");
  }

  @Test
  public void mipsTestH() {
    runMIPSTest("test3h");
  }

  @Test
  public void mipsTestI() {
    runMIPSTest("test3i");
  }

  @Test
  public void mipsTestJ() {
    runMIPSTest("test3j");
  }

  @Test
  public void mipsTestK() {
    runMIPSTest("test3k");
  }

  @Test
  public void mipsTestL() {
    runMIPSTest("test3l");
  }

  @Test
  public void mipsTestM() {
    runMIPSTest("test3m");
  }

  @Test
  public void mipsTestN() {
    runMIPSTest("test3n");
  }
  
  @Test
  public void myTest1() {
    runMIPSTest("test_joshuacrotts_1");
  }
  
  @Test
  public void myTest2() {
    runMIPSTest("test_joshuacrotts_2");
  }
  
  @Test
  public void myTest3() {
    runMIPSTest("test_joshuacrotts_3");
  }
  
  @Test
  public void myTest4() {
    runMIPSTest("test_joshuacrotts_4");
  }
  
  @Test
  public void myTest5() {
    runMIPSTest("test_joshuacrotts_5");
  }
  
  @Test
  public void myTest6() {
    runMIPSTest("test_joshuacrotts_6");
  }
  
  @Test
  public void myTest7() {
    runMIPSTest("test_joshuacrotts_7");
  }
}
