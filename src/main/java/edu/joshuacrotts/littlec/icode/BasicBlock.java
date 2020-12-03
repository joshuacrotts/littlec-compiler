package edu.joshuacrotts.littlec.icode;

import java.util.LinkedList;

/**
 * Basic block as defined in the dragon book. This will hopefully be helpful
 * when doing code generation.
 * 
 * @author Joshua Crotts
 */
public class BasicBlock {

  /**
   * Each inner linked list is a component of the 3AC. The outer-linked list is
   * the list of 3ACs.
   */
  private final LinkedList<LinkedList<String>> INSTRUCTIONS;

  public BasicBlock() {
    this.INSTRUCTIONS = new LinkedList<>();
  }

  /**
   * Static method to determine if the passed string is where we end a basic
   * block. Basic blocks are terminated by jump instructions. These include gotos,
   * returns, calls, and function ends (all of which handle flow of control). The
   * instruction should be added to the current BB, then a new one is generated.
   * 
   * @param str - result or operator of 3AC.
   * 
   * @return true if we end the basic block, false otherwise.
   */
  public static boolean isEndOfBasicBlock(String str) {
    return str.contains("goto") || str.contains("return") || str.contains("call") || str.contains(".fnEnd");
  }

  /**
   * Static method to determine if the passed string is where we begin a basic
   * block. The only time we can directly create a basic block is when we're at a
   * label.
   * 
   * @param str
   * 
   * @return true if we start a basic block, false otherwise.
   */
  public static boolean isStartOfBasicBlock(String str) {
    return str.contains(":");
  }

  /**
   * Adds a new three-address code instruction to the end of our basic block. Each
   * three-address line has a LinkedList dedicated to it so we can extract
   * information without having to parse a string.
   * 
   * @param res - result of three-address code.
   * @param op1 - operand 1.
   * @param op2 - operand 2.
   * @param op  - operator used.
   * 
   * @return void.
   */
  public void addInstruction(String res, String op1, String op2, String op) {
    this.addInstruction(this.INSTRUCTIONS.size(), res, op1, op2, op);
  }

  /**
   * Adds a new three-address code instruction to our basic block. Each
   * three-address line has a LinkedList dedicated to it so we can extract
   * information without having to parse a string. This particular method stores
   * the instruction at a specific index in the basic block.
   * 
   * @param idx - index to store the basic block (line number).
   * @param res - result of three-address code.
   * @param op1 - operand 1.
   * @param op2 - operand 2.
   * @param op  - operator used.
   * 
   * @return void.
   */
  public void addInstruction(int idx, String res, String op1, String op2, String op) {
    LinkedList<String> tac = new LinkedList<>();
    tac.add(res);
    tac.add(op1);
    tac.add(op2);
    tac.add(op);
    this.INSTRUCTIONS.add(idx, tac);
  }

  /**
   * Returns true if this address is a three-address instruction, meaning that
   * there are two operands, one operator, and one destination address.
   * 
   * @return true if the res, op1, op2, and op indices are not empty. False
   *         otherwise.
   */
  public boolean is3Address(LinkedList<String> instructions) {
    return !instructions.get(0).isEmpty() && !instructions.get(1).isEmpty() && !instructions.get(2).isEmpty()
        && !instructions.get(3).isEmpty();
  }

  /**
   * Returns true if this address is a two-address instruction, meaning that there
   * is one operand, one operator, and one destination address.
   * 
   * @return true if the res, op1, and op indices are not empty. False otherwise.
   */
  public boolean is2Address(LinkedList<String> instructions) {
    return !instructions.get(0).isEmpty() && !instructions.get(1).isEmpty() && instructions.get(2).isEmpty()
        && !instructions.get(3).isEmpty();
  }

  public boolean isEmpty() {
    return this.INSTRUCTIONS.isEmpty();
  }

  public LinkedList<LinkedList<String>> getInstructions() {
    return this.INSTRUCTIONS;
  }

  public LinkedList<String> getCurrentInstruction(int idx) {
    return this.INSTRUCTIONS.get(idx);
  }

  public int getNumberOfInstructions() {
    return this.INSTRUCTIONS.size();
  }

  /**
   * Generates a string representation of the basic blocks. Each basic block
   * prints a table with the 3 address code instructions that belong to said
   * block.
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("%10s | %30s | %30s | %30s | %30s |\n", "Line #", "Res. Addr.", "Op1. Addr.", "Op2. Addr.",
        "Op"));
    sb.append(String.format(
        "------------------------------------------------------------------------------------------------------------------------------------------------\n"));
    for (int i = 0; i < this.INSTRUCTIONS.size(); i++) {
      LinkedList<String> line = this.INSTRUCTIONS.get(i);
      sb.append(String.format("%10d | %30s | %30s | %30s | %30s |\n", (i + 1), line.get(0), line.get(1), line.get(2),
          line.get(3)));
      sb.append(
          "------------------------------------------------------------------------------------------------------------------------------------------------\n");
    }

    return sb.toString();
  }
}
