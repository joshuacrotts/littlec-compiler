package edu.joshuacrotts.littlec.main;

import org.antlr.v4.runtime.BaseErrorListener;
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
  private boolean gotError;

  public LCErrorListener() {
    super();
    gotError = false;
  }

  @Override
  public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
      String msg, RecognitionException e) {
    gotError = true;
  }

  /**
   * Was an error encountered?
   * 
   * @return true if an error was seen.
   */
  public boolean sawError() {
    return gotError;
  }
}
