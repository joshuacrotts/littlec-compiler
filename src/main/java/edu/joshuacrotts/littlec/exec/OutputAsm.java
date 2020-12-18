package edu.joshuacrotts.littlec.exec;
import java.io.IOException;
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
 * Compile a LittleC program to intermediate code, printing the IC to standard
 * output.
 *
 * @author Steve Tate (srtate@uncg.edu)
 */
public class OutputAsm {
  
  /**
   * Runs the parser and edu.joshuacrotts.LCListener syntax tree constructor for
   * the provided input stream. The returned object can be used to access the
   * syntax tree and the symbol table for either futher processing or for checking
   * results in automated tests.
   *
   * @param input an initialized CharStream
   * @return the edu.joshuacrotts.LCListener object that processed the parsed input
   *         or null if an error was encountered
   */
  private static LCListener parseStream(CharStream input) {
    // "input" is the character-by-character input - connect to lexer
    LittleCLexer lexer = new LittleCLexer(input);
    LCErrorListener catchErrs = new LCErrorListener();
    lexer.removeErrorListeners();
    lexer.addErrorListener(catchErrs);

    // Connect token stream to lexer
    CommonTokenStream tokens = new CommonTokenStream(lexer);

    // Connect parser to token stream
    LittleCParser parser = new LittleCParser(tokens);
    parser.removeErrorListeners();
    parser.addErrorListener(catchErrs);
    ParseTree tree = parser.program();

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
   * @return the edu.joshuacrotts.LCListener object that processed the parsed input
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
   * @return the edu.joshuacrotts.LCListener object that processed the parsed input
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
      System.out.println(asmOut);
    }
  }
}
