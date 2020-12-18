package edu.joshuacrotts.littlec.main;

import java.util.LinkedHashSet;
import java.util.Set;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

/**
 * This class can be added to either the lexer or the parser error reporting
 * chains (or both). All it does is keep track of whether an error was detected,
 * so after parsing you can call sawError() to see if there was a problem.
 *
 * @author Steve Tate
 */
public class LCErrorListener extends BaseErrorListener {

  private static Set<String> errors = new LinkedHashSet<>();
  private static Set<String> warnings= new LinkedHashSet<>();

  private static boolean gotError = false;
  private static boolean gotWarning = false;

  public LCErrorListener() {
    super();
  }

  /**
   * Prints an error message to the console with the line and column number
   * specified by the ParserRuleContext. The error flag is also set.
   * 
   * @param ctx
   * @param errorMsg
   */
  public static void syntaxError(ParserRuleContext ctx, String errorMsg) {
    LCErrorListener.gotError = true;
    int lineNo = -1;
    int colNo = -1;
    
    if (ctx != null) {
      lineNo = ctx.start.getLine();
      colNo = ctx.start.getCharPositionInLine();
    } else {
      throw new IllegalArgumentException("Internal compiler error - ParserRuleContext cannot be null in ErrorListener.");
    }
    
    LCErrorListener.errors.add("line " + lineNo + ":" + colNo + " " + errorMsg);
  }

  /**
   * Prints an warning message to the console with the line and column number
   * specified by the ParserRuleContext.
   * 
   * @param ctx
   * @param errorMsg
   * 
   * @return void.
   */
  public static void syntaxWarning(ParserRuleContext ctx, String warningMsg) {
    LCErrorListener.gotWarning = true;
    int lineNo = -1;
    int colNo = -1;
    
    if (ctx != null) {
      lineNo = ctx.start.getLine();
      colNo = ctx.start.getCharPositionInLine();
    } else {
      throw new IllegalArgumentException("Internal compiler error - ParserRuleContext cannot be null in ErrorListener.");
    }
    
    LCErrorListener.errors.add("line " + lineNo + ":" + colNo + " " + warningMsg);
  }
  
  /**
   * Prints error messages generated through parsing the syntax tree to
   * standard error.
   * 
   * @param void.
   * 
   * @return void.
   */
  public static void printErrors() {
    System.err.print("ERRORS(" + LCErrorListener.errors.size() + "):\n");
    for (String error : LCErrorListener.errors) {
      System.err.println(error);
    }
  }

  /**
   * Prints warning messages generated through parsing the syntax tree to
   * standard out.
   * 
   * @param void.
   * 
   * @return void.
   */
  public static void printWarnings() {
    System.out.print("WARNINGS(" + LCErrorListener.warnings.size() + "):\n");
    for (String warning : LCErrorListener.warnings) {
      System.out.println(warning);
    }
  }

  /**
   * Was an error encountered?
   * 
   * @return true if an error was seen.
   */
  public static boolean sawError() {
    return gotError;
  }
  
  /**
   * Was a warning encountered? This probably serves little use.
   * 
   * @return true if a warning was seen.
   */
  public static boolean sawWarning() {
    return gotError;
  }
  
  /**
   * 
   */
  @Override
  public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
      String errorMsg, RecognitionException e) {
    gotError = true;
    LCErrorListener.errors.add("line " + line + ":" + charPositionInLine + " " + errorMsg);
  }
}
