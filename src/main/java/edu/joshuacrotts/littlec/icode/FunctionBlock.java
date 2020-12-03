package edu.joshuacrotts.littlec.icode;

import java.util.LinkedList;

public class FunctionBlock {

  /** List of instructions for this function. */
  private final LinkedList<LinkedList<String>> INSTRUCTIONS;

  public FunctionBlock() {
    this.INSTRUCTIONS = new LinkedList<>();
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
   * Adds a new three-address code instruction to our function block. Each
   * three-address line has a LinkedList dedicated to it so we can extract
   * information without having to parse a string. This particular method stores
   * the instruction at a specific index in the function block.
   * 
   * @param idx - index to store the function block (line number).
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
   * Generates the basic blocks for a function block.
   * 
   * @param void.
   * 
   * @return LinkedList of basic blocks.
   */
  public LinkedList<BasicBlock> generateFunctionBasicBlocks() {
    LinkedList<BasicBlock> basicBlocks = new LinkedList<>();
    basicBlocks.add(new BasicBlock());

    for (int i = 0, j = 0; i < this.INSTRUCTIONS.size(); i++) {
      for (j = i; j < this.INSTRUCTIONS.size(); j++) {
        LinkedList<String> currInst = this.INSTRUCTIONS.get(i);
        String res = currInst.get(0);
        String op1 = currInst.get(1);
        String op2 = currInst.get(2);
        String op = currInst.get(3);

        // First we handle if we're at the target of a jump.
        if (BasicBlock.isStartOfBasicBlock(res)) {
          // If the basic block is empty then we can just use it instead of adding a new
          // one.
          if (!basicBlocks.peekLast().isEmpty()) {
            basicBlocks.add(new BasicBlock());
          }
          basicBlocks.peekLast().addInstruction(res, op1, op2, op);
          break;
        }
        // Now we handle if we're about to jump.
        else if (BasicBlock.isEndOfBasicBlock(op) || BasicBlock.isEndOfBasicBlock(res)) {
          basicBlocks.peekLast().addInstruction(res, op1, op2, op);
          // If we're not about to stop generating code, we can add a new BB.
          if (j + 1 < this.INSTRUCTIONS.size()) {
            basicBlocks.add(new BasicBlock());
          }

          break;
        }

        // Otherwise , just add the BB as normal.
        basicBlocks.peekLast().addInstruction(res, op1, op2, op);
        i++;
      }
      i = j;
    }

    return basicBlocks;
  }

  /**
   * Returns the list of instructions used in this function block.
   * 
   * @param void.
   * 
   * @return LinkedList of LinkedList of String instructions.
   */
  public LinkedList<LinkedList<String>> getInstructions() {
    return this.INSTRUCTIONS;
  }

  /**
   * Returns the function name without the gf_ prefix.
   * 
   * @param void.
   * 
   * @return string name of function.
   */
  public String getFunctionName() {
    String name = this.INSTRUCTIONS.get(0).get(0);
    return name.substring(name.indexOf("_") + 1);
  }

  /**
   * Generates a string representation of the function blocks. Each function block
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
