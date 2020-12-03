package edu.joshuacrotts.littlec.main;

/**
 * These masks are used to determine the state of a LCSyntaxTree or a portion of
 * the tree.
 * 
 * @author Joshua Crotts
 */
public class LCMasks {

  /* Assign mask is toggled if we enter an assignment statement. */
  public static final int ASSIGN_MASK = 0x00000001;

  /* Return mask is toggled once we enter a return statement. */
  public static final int RETURN_MASK = 0x00000002;

  /* Loop mask is toggled if we enter a loop, whether it be a for or while. */
  public static final int LOOP_MASK = 0x00000004;

  /* If mask is toggled if we enter an if statement. */
  public static final int IF_MASK = 0x0000008;

  /* Function mask is toggled if we enter a function call. */
  public static final int FUNCTION_CALL_MASK = 0x00000010;

  /*
   * This flag is toggled if we're inside an expression and performing an
   * assignment stmt.
   */
  public static final int EXPR_ASSIGN_MASK = 0x00000020;

  /*
   * Multi-line mask is toggled if we enter a statement (if/for/while) with braces
   * (i.e. it has multiple lines).
   */
  public static final int MULTI_LINE_MASK = 0x00001000;

  /*
   * Cond mask is toggled if we enter the conditional of an if/for/while
   * statement.
   */
  public static final int COND_MASK = 0x00002000;

  /* Void mask is toggled if the function we declare returns nothing. */
  public static final int RETURN_VOID_MASK = 0x00100000;

  /* Int mask is toggled if the function we declare returns an int. */
  public static final int RETURN_INT_MASK = 0x00200000;

  /* Char mask is toggled if the function we declare returns a char. */
  public static final int RETURN_CHAR_MASK = 0x00400000;

  /*
   * Error mask is toggled whenever an error is encountered by the semantic
   * checks. If this flag is set, then all other listeners terminate immediately.
   */
  public static final int ERROR_MASK = 0x80000000;
}
