package edu.joshuacrotts.littlec.exec;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.NoSuchFileException;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import edu.joshuacrotts.littlec.antlr4.LittleCLexer;
import edu.joshuacrotts.littlec.antlr4.LittleCParser;
import edu.joshuacrotts.littlec.icode.ICode;
import edu.joshuacrotts.littlec.main.LCErrorListener;
import edu.joshuacrotts.littlec.main.LCListener;
import edu.joshuacrotts.littlec.mipsgen.MIPSGen;
import edu.joshuacrotts.littlec.syntaxtree.LCSyntaxTree;

/**
 * Compile a LittleC program to MIPS assembly, then runs the SPIM command to
 * execute the output. This allows us to forego using QTSpim whenever we want
 * to just run the program to verify output.
 * 
 * @author Joshua Crotts
 */
public class RunMIPS {

  /**
   * Runs the parser and edu.joshuacrotts.LCListener syntax tree constructor for
   * the provided input stream. The returned object can be used to access the
   * syntax tree and the symbol table for either futher processing or for checking
   * results in automated tests.
   *
   * @param input an initialized CharStream
   * @return the edu.joshuacrotts.LCListener object that processed the parsed
   *         input or null if an error was encountered
   */
  private static LCListener parseStream(CharStream input) {
    // "input" is the character-by-character input - connect to lexer
    LittleCLexer lexer = new LittleCLexer(input);
    LCErrorListener catchErrs = new LCErrorListener();
    lexer.addErrorListener(catchErrs);

    // Connect token stream to lexer
    CommonTokenStream tokens = new CommonTokenStream(lexer);

    // Connect parser to token stream
    LittleCParser parser = new LittleCParser(tokens);
    parser.addErrorListener(catchErrs);
    ParseTree tree = parser.program();
    if (catchErrs.sawError())
      return null;

    // Now do the parsing, and walk the parse tree with our listeners
    ParseTreeWalker walker = new ParseTreeWalker();
    LCListener compiler = new LCListener(parser);
    walker.walk(compiler, tree);
    return compiler;
  }

  /**
   * Public static method to run the parser on an input file.
   *
   * @param fileName the name of the file to use for input
   * @return the edu.joshuacrotts.LCListener object that processed the parsed
   *         input
   */
  public static LCListener parseFromFile(String fileName) {
    try {
      return parseStream(CharStreams.fromFileName(fileName));
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
   * Public static method to run the parser on the standard input stream.
   *
   * @return the edu.joshuacrotts.LCListener object that processed the parsed
   *         input
   */
  public static LCListener parseFromStdin() {
    try {
      return parseStream(CharStreams.fromStream(System.in));
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
    LCListener parser;
    if (argv.length > 1) {
      System.err.println("Can provide at most one command line argument (an input filename)");
      return;
    } else if (argv.length == 1) {
      parser = parseFromFile(argv[0]);
    } else {
      parser = parseFromStdin();
    }

    LCSyntaxTree result = null;
    if (parser != null)
      result = parser.getSyntaxTree();

    if (result != null) {
      ICode iCode = new ICode(result);
      MIPSGen asmOut = new MIPSGen(iCode);

      // Write the data to a file.
      generateLCFile(asmOut);

      // Build the process and output info.
      generateOutput();

      // Delete the file that we created temporarily.
      deleteLCFile();
    }
  }

  /**
   * 
   * @param asmOut
   */
  private static void generateLCFile(MIPSGen asmOut) {
    BufferedWriter fos = null;
    try {
      fos = new BufferedWriter(new FileWriter("file.lc"));
      fos.write(asmOut.toString());
      fos.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * 
   */
  private static void generateOutput() {
    // Start a new process.
    ProcessBuilder processBuilder = new ProcessBuilder();

    // First we need to verify that we are running the appropriate
    // command on the OS of choice.
    String os = System.getProperty("os.name");
    if (os.contains("Mac") || os.contains("nix"))
      processBuilder.command("/usr/local/bin/spim", "load", "file.lc");
    else
      throw new UnsupportedOperationException("Windows is not supported yet.");

    // Now, we start running the process.
    try {
      Process process = processBuilder.start();

      // Build the output.
      StringBuilder output = new StringBuilder();
      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

      // The first line contains the command that verifies
      // that we loaded the program correctly.
      reader.readLine();

      // Append each line to stdout.
      String line;
      while ((line = reader.readLine()) != null) {
        output.append(line + "\n");
      }

      int exitVal = process.waitFor();
      if (exitVal == 0) {
        System.out.println(output);
      } else {
        throw new RuntimeException(
            "SPIM could not be executed - is the program installed correctly? Make sure the path is correct.");
      }
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  /**
   * 
   */
  private static void deleteLCFile() {
    File f = new File("file.lc");
    if (!f.delete()) {
      throw new IllegalStateException("Temporary LC file deleted unsuccessfully.");
    }
  }
}
