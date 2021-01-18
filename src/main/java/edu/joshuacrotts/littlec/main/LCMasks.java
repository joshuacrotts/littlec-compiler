package edu.joshuacrotts.littlec.main;

/**
 * These masks are used to determine the state of a LCSyntaxTree or a portion of
 * the tree.
 * 
 * @author Joshua Crotts
 */
public class LCMasks {

  /** Assign mask is toggled if we enter an assignment statement. */
  public static final int ASSIGN_MASK = 0x00000001;

  /** Return mask is toggled once we enter a return statement. */
  public static final int RETURN_MASK = 0x00000002;

  /** Loop mask is toggled if we enter a loop, whether it be a for or while. */
  public static final int LOOP_MASK = 0x00000004;

  /** If mask is toggled if we enter an if statement. */
  public static final int IF_MASK = 0x0000008;

  /** Function mask is toggled if we enter a function call. */
  public static final int FUNCTION_CALL_MASK = 0x00000010;

  /**
   * This flag is toggled if we're inside an expression and performing an
   * assignment stmt.
   */
  public static final int EXPR_ASSIGN_MASK = 0x00000020;

  /**
   * Cond mask is toggled if we enter the conditional of an if/for/while
   * statement.
   */
  public static final int COND_MASK = 0x00002000;

  /** */
  public static final int WARNING_MASK = 0x40000000;
}
