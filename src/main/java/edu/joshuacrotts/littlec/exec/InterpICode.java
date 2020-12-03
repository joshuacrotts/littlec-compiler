package edu.joshuacrotts.littlec.exec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.LinkedList;

import edu.joshuacrotts.littlec.icinterp.ICInterp;

/**
 * Program to interpret intermediate code. This is normally done as part of the
 * RunCode program, but is provided separately so that users can test
 * hand-generated (or modified) intermediate code.
 *
 * @author Steve Tate (srtate@uncg.edu)
 */
public class InterpICode {
  /**
   * Public static method to read input from a file
   *
   * @param fileName the name of the file to use for input
   * @return a string containing full file contents
   */
  public static String readFromFile(String fileName) {
    try {
      return new String(Files.readAllBytes(Paths.get(fileName)));
    } catch (IOException e) {
      if (e instanceof NoSuchFileException) {
        System.err.println("Could not open file " + fileName);
      } else {
        e.printStackTrace();
      }
    }
    return null;
  }

  /**
   * Public static method to read input from the standard input stream.
   *
   * @return a string containing full input contents
   */
  public static String readFromStdin() {
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      byte[] buffer = new byte[32 * 1024];

      int bytesRead;
      while ((bytesRead = System.in.read(buffer)) > 0) {
        baos.write(buffer, 0, bytesRead);
      }
      return new String(baos.toByteArray());
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Command line interface -- one argument is filename, and if omitted then input
   * is taken from standard input.
   *
   * @param argv command line arguments
   */
  public static void main(String[] argv) {
    String iCode;
    if (argv.length > 1) {
      System.err.println("Can provide at most one command line argument (an input filename)");
      return;
    } else if (argv.length == 1) {
      iCode = readFromFile(argv[0]);
    } else {
      iCode = readFromStdin();
    }

    if (iCode != null) {
      ICInterp context = new ICInterp(iCode);
      context.executeFunction("gf_main", new LinkedList<>());
    }
  }
}
