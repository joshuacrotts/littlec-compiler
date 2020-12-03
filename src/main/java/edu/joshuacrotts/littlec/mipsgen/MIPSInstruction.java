package edu.joshuacrotts.littlec.mipsgen;

/**
 * This class has several helper methods that generate the MIPS code for the
 * CodeGeneration class. Each instruction (except for labels) have tabs appended
 * to the front.
 * 
 * @author Joshua Crotts
 */
public class MIPSInstruction {

  /**
   * Enum for all supported MIPS operations. Not all are used, though.
   */
  private enum MIPS {
    sw, sb, move, lw, li, lb, la, ld, sll, b, j, addu, subu, div, mul, jal, jr;
  }

  /**
   * Tab character.
   */
  private static final char TAB = '\t';

  /**
   * Space character.
   */
  private static final char SPACE = ' ';

  /**
   * New-line character.
   */
  private static final char NEWLINE = '\n';

  /**
   * Separator for instructions with multiple operands (or a destination and an
   * operand).
   */
  private static final String SEP = ", ";

  /**
   * Generates a move instruction.
   * 
   * @param dest - register to store result in.
   * @param src  - register to move result from.
   * 
   * @return String representation of MIPS instruction.
   */
  protected static String genMove(MIPSReg dest, MIPSReg src) {
    StringBuilder sb = new StringBuilder();

    if (dest.equals(src)) {
      return "";
    }

    sb.append(TAB);
    sb.append(MIPS.move);
    sb.append(SPACE);
    sb.append(dest);
    sb.append(SEP);
    sb.append(src);
    sb.append(NEWLINE);

    return sb.toString();
  }

  /**
   * Generates a load instruction.
   * 
   * @param loadType - load instruction to use.
   * @param reg      - register to store result in.
   * @param src      - memory location to load instruction from.
   * 
   * @return String representation of MIPS instruction.
   */
  protected static String genLoad(String loadType, MIPSReg reg, String src) {
    StringBuilder sb = new StringBuilder();

    sb.append(TAB);
    sb.append(loadType);
    sb.append(SPACE);
    sb.append(reg);
    sb.append(SEP);
    sb.append(src);
    sb.append(NEWLINE);

    return sb.toString();
  }

  /**
   * Generates a store instruction.
   * 
   * @param storeType - store instruction to use.
   * @param reg       - register to pull result from.
   * @param src       - memory location to store instruction in.
   * 
   * @return String representation of MIPS instruction.
   */
  protected static String genStore(String storeType, MIPSReg src, String dest) {
    StringBuilder sb = new StringBuilder();

    sb.append(TAB);
    sb.append(storeType);
    sb.append(SPACE);
    sb.append(src);
    sb.append(SEP);
    sb.append(dest);
    sb.append(NEWLINE);

    return sb.toString();
  }

  /**
   * Generates a jump instruction ("jal").
   * 
   * @param functionName - function to jump to.
   * 
   * @return String representation of MIPS instruction.
   */
  protected static String genFunctionCall(String functionName) {
    StringBuilder sb = new StringBuilder();

    sb.append(TAB);
    sb.append(MIPS.jal);
    sb.append(SPACE);
    sb.append(functionName);
    sb.append(NEWLINE);

    return sb.toString();
  }

  /**
   * Generates a binary operator in MIPS.
   * 
   * @param dest - destination.
   * @param op1  - first operand.
   * @param op2  - second operand.
   * 
   * @return String representation of MIPS instruction.
   */
  protected static String genBinaryOp(String op, String dest, String op1, String op2) {
    StringBuilder sb = new StringBuilder();

    sb.append(TAB);
    sb.append(op);
    sb.append(SPACE);
    sb.append(dest);
    sb.append(SEP);
    sb.append(op1);
    sb.append(SEP);
    sb.append(op2);
    sb.append(NEWLINE);

    return sb.toString();
  }

  /**
   * Generates a binary operator in MIPS.
   * 
   * @param dest - destination.
   * @param op1  - first operand.
   * 
   * @return String representation of MIPS instruction.
   */
  protected static String genUnaryOp(String op, String dest, String op1) {
    StringBuilder sb = new StringBuilder();

    sb.append(TAB);
    sb.append(op);
    sb.append(SPACE);
    sb.append(dest);
    sb.append(SEP);
    sb.append(op1);
    sb.append(NEWLINE);

    return sb.toString();
  }

  /**
   * Generates a branch instruction ("b") in MIPS.
   * 
   * @param dest location to branch to.
   * 
   * @return String representation of MIPS instruction.
   */
  protected static String genBranch(String dest) {
    StringBuilder sb = new StringBuilder();

    sb.append(TAB);
    sb.append(MIPS.b);
    sb.append(SPACE);
    sb.append(dest);
    sb.append(NEWLINE);

    return sb.toString();
  }

  /**
   * Generates a jump instruction ("j") in MIPS.
   * 
   * @param dest location to jump to.
   * 
   * @return String representation of MIPS instruction.
   */
  protected static String genJump(String label) {
    StringBuilder sb = new StringBuilder();

    sb.append(TAB);
    sb.append(MIPS.j);
    sb.append(SPACE);
    sb.append(label);
    sb.append(NEWLINE);

    return sb.toString();
  }

  /**
   * Generates a label in the form LD: where D is a number.
   * 
   * @param label - string jump label.
   * 
   * @return String representation of MIPS instruction.
   */
  protected static String genLabel(String label) {
    StringBuilder sb = new StringBuilder();

    sb.append(label);
    sb.append(NEWLINE);

    return sb.toString();
  }
}
