package edu.joshuacrotts.littlec.icode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * This class represents the internal structure of an activation record by
 * keeping its 3AC in separate ArrayLists that are all aligned.
 *
 * @author Joshua
 */
public class Quadruple {

  /** Result list - stores the lvalue for a 3AC. */
  private final ArrayList<String> RES_LIST;

  /** First operand of a 3AC instruction. */
  private final ArrayList<String> OPERAND1_LIST;

  /** Second operand of a 3AC instruction. */
  private final ArrayList<String> OPERAND2_LIST;

  /** Operator of a 3AC instruction. */
  private final ArrayList<String> OPERATOR_LIST;

  /** Set of labels currently stored. */
  private final Set<String> LABELS;

  public Quadruple() {
    this.RES_LIST = new ArrayList<>();
    this.OPERAND1_LIST = new ArrayList<>();
    this.OPERAND2_LIST = new ArrayList<>();
    this.OPERATOR_LIST = new ArrayList<>();
    this.LABELS = new HashSet<>();
  }

  /**
   * Adds a line of code to the quadruple table. All fields are used in this
   * method.
   * 
   * @param resAddr
   * @param operand1
   * @param operand2
   * @param op
   */
  public void addLine(String resAddr, String operand1, String operand2, String op) {
    this.RES_LIST.add(resAddr);
    this.OPERAND1_LIST.add(operand1);
    this.OPERAND2_LIST.add(operand2);
    this.OPERATOR_LIST.add(op);

    if (!verifySize()) {
      throw new RuntimeException("Quadruple in activation record is misaligned.");
    }
  }

  /**
   * Adds a line of code to the quadruple table. All fields are used in this
   * method.
   * 
   * @param lineNo
   * @param resAddr
   * @param operand1
   * @param operand2
   * @param op
   */
  public void addLine(int lineNo, String resAddr, String operand1, String operand2, String op) {
    this.RES_LIST.add(lineNo, resAddr);
    this.OPERAND1_LIST.add(lineNo, operand1);
    this.OPERAND2_LIST.add(lineNo, operand2);
    this.OPERATOR_LIST.add(lineNo, op);

    if (!verifySize()) {
      throw new RuntimeException("Quadruple is misaligned.");
    }
  }

  /**
   * Adds a line of code to the quadruple table. Only one operand is used in this
   * version of the method.
   *
   * @param lineNo
   * @param resAddr
   * @param operand
   * @param op
   */
  public void addLine(int lineNo, String resAddr, String operand, String op) {
    this.addLine(lineNo, resAddr, operand, "", op);
  }

  /**
   * Adds a line of code to the quadruple table. Only one operand is used in this
   * version of the method.
   *
   * @param resAddr
   * @param operand
   * @param op
   */
  public void addLine(String resAddr, String operand, String op) {
    this.addLine(resAddr, operand, "", op);
  }

  /**
   * Adds a resulting address and a single-operand to the quad at a specific line.
   * 
   * @param lineNo
   * @param resAddr
   * @param operand
   */
  public void addLine(int lineNo, String resAddr, String operand) {
    this.addLine(lineNo, resAddr, operand, "");
  }

  /**
   * Adds a resulting address and a single-operand to the quad.
   * 
   * @param resAddr
   * @param operand
   */
  public void addLine(String resAddr, String operand) {
    this.addLine(resAddr, operand, "");
  }

  /**
   * Adds a label to the result list of the quadruple at a specific line number.
   * 
   * @param lineNo
   * @param label
   */
  public void addLabel(int lineNo, String label) {
    this.addLine(lineNo, label, "", "");
  }

  /**
   * Adds a label to the result list of the quadruple. If you want to designate a
   * destination label (e.g. "L1:" with a colon), just concatenate it on the end.
   * 
   * @param label
   */
  public void addLabel(String label) {
    this.addLine(label, "", "");
  }

  /**
   * Adds a non-void function to the quadruple. A resulting temporary variable
   * stores the return value of the function.
   * 
   * @param resAddr  - temporary variable (return value of function).
   * @param fnCall   - function call (name)
   * @param argCount - number of arguments supplied to the function.
   */
  public void addFunctionCall(String resAddr, String fnCall, int argCount) {
    this.addLine(resAddr, fnCall, Integer.toString(argCount), "call");
  }

  /**
   * Adds a void function call to the quadruple. Void functions do not have a
   * return address and are called on their own line.
   * 
   * @param fnCall
   * @param argCount
   */
  public void addVoidFunctionCall(String fnCall, int argCount) {
    this.addLine("", fnCall, Integer.toString(argCount), "call");
  }

  /**
   * Adds a cast operation to the quad.
   * 
   * @param resAddr    - resulting address of cast op.
   * @param castRValue - operand to cast.
   * @param castOp     - type of casting operator.
   */
  public void addCast(String resAddr, String castRValue, String castOp) {
    this.addLine(resAddr, castRValue, castOp);
  }

  /**
   * Returns the next available line of code in the table.
   * 
   * @return int next line.
   */
  public int getNextAvailableLine() {
    return this.RES_LIST.size();
  }

  /**
   * Clears the operators and operands and results in the table. This is generally
   * used for JUnit tests...
   */
  public void cleanup() {
    RES_LIST.clear();
    OPERAND1_LIST.clear();
    OPERAND2_LIST.clear();
    OPERATOR_LIST.clear();
  }

  /**
   * Prints a table-view of the quadruple in rows.
   * 
   * The columns printed are line number, resulting address, operand 1, operand 2,
   * and operator.
   */
  public void printTable() {
    System.out.printf("%10s | %30s | %30s | %30s | %30s |\n", "Line #", "Res. Addr.", "Op1. Addr.", "Op2. Addr.", "Op");
    System.out.printf(
        "------------------------------------------------------------------------------------------------------------------------------------------------\n");
    for (int i = 0; i < RES_LIST.size(); i++) {
      System.out.printf("%10d | %30s | %30s | %30s | %30s |\n", (i + 1), RES_LIST.get(i), OPERAND1_LIST.get(i),
          OPERAND2_LIST.get(i), OPERATOR_LIST.get(i));
      System.out.printf(
          "------------------------------------------------------------------------------------------------------------------------------------------------\n");
    }
  }

  /**
   * Generates the function blocks for intermediate-code. A new function block is
   * generated in between a gf_ label, and a .fnEnd label.
   * 
   * We traverse the list of instructions, adding them to the current FB, until
   * the end is encountered.
   * 
   * @param void.
   * @return LinkedList of FunctionBlock objects.
   */
  public LinkedList<FunctionBlock> generateFunctionBlocks() {
    LinkedList<FunctionBlock> functionBlocks = new LinkedList<>();
    for (int i = 0, j = 0; i < RES_LIST.size(); i++) {
      String res = RES_LIST.get(i);
      if (res.contains("gf_")) {
        functionBlocks.add(new FunctionBlock());
        for (j = i; j < RES_LIST.size(); j++) {
          res = RES_LIST.get(j);
          String op1 = OPERAND1_LIST.get(j);
          String op2 = OPERAND2_LIST.get(j);
          String op = OPERATOR_LIST.get(j);

          functionBlocks.peekLast().addInstruction(res, op1, op2, op);

          if (res.contains(".fnEnd")) {
            break;
          }
        }
      }
    }

    return functionBlocks;
  }

  /**
   * Generates a LinkedList of LinkedLists of String instructions (3AC) for every
   * global declaration in the Quadruple.
   * 
   * @param void.
   * 
   * @return LinkedList<LinkedList<String>> where the inner linked-list represents
   *         the 3 address codes, and the outer represents each row of 3AC.
   */
  public LinkedList<LinkedList<String>> getGlobalVariableDeclarations() {
    LinkedList<LinkedList<String>> globals = new LinkedList<>();

    for (int i = 0; i < RES_LIST.size(); i++) {
      LinkedList<String> currInst = new LinkedList<>();
      String res = RES_LIST.get(i);
      String op1 = OPERAND1_LIST.get(i);
      String op2 = OPERAND2_LIST.get(i);

      // Iterate through and find all global var declarations.
      if (res.contains("g") && !res.contains("gf_") && !res.contains("goto") && op1.contains(".d")) {
        currInst.add(res);
        currInst.add(op1);
        currInst.add(op2);
        globals.add(currInst);

        // If we are on an array, we need to populate it.
        if (res.contains("g0")) {
          int j = i + 1;
          for (j = i + 1; j < RES_LIST.size(); j++) {
            currInst = new LinkedList<>();
            res = RES_LIST.get(j);
            op1 = OPERAND1_LIST.get(j);
            op2 = OPERAND2_LIST.get(j);
            // Once we find a new declaration, stop traversing.
            if (!res.contains(".dw") && !res.contains(".db")) {
              break;
            }
            currInst.add(res);
            currInst.add(op1);
            currInst.add(op2);
            globals.add(currInst);
          }
          i = j - 1;
        }
      }
    }

    return globals;
  }

  /**
   * Prints out the function blocks in the list parameter. Each function block has
   * a number associated, along with the instructions.
   * 
   * @param LinkedList of function blocks.
   * 
   * @return void.
   */
  public void printFunctionBlocks(LinkedList<FunctionBlock> functionBlocks) {
    for (int i = 0; i < functionBlocks.size(); i++) {
      System.out.printf("Function Block %d:\n", i + 1);
      System.out.println(functionBlocks.get(i));
    }
  }

  /**
   * Generates the basic blocks for intermediate-code. A new basic block is
   * generated via one of three scenarios:
   * 
   * 1. The code is the first instruction. 2. The code is the target of a jump
   * (including returns, calls, and end of functions.). 3. The code immediately
   * follows a jump.
   * 
   * We traverse the list of instructions, adding them to the current BB, until
   * one of these scenarios is encountered.
   * 
   * @param void.
   * @return LinkedList of BasicBlock objects.
   */
  public LinkedList<BasicBlock> generateBasicBlocks() {
    LinkedList<BasicBlock> basicBlocks = new LinkedList<>();
    basicBlocks.add(new BasicBlock());

    for (int i = 0, j = 0; i < RES_LIST.size(); i++) {
      for (j = i; j < RES_LIST.size(); j++) {
        String res = RES_LIST.get(j);
        String op1 = OPERAND1_LIST.get(j);
        String op2 = OPERAND2_LIST.get(j);
        String op = OPERATOR_LIST.get(j);

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
          if (j + 1 < RES_LIST.size()) {
            basicBlocks.add(new BasicBlock());
          }

          break;
        }

        // Otherwise, just add the BB as normal.
        basicBlocks.peekLast().addInstruction(res, op1, op2, op);
      }

      i = j;
    }

    return basicBlocks;
  }

  /**
   * Prints out the basic blocks in the list parameter. Each basic block has a
   * number associated, along with the instructions.
   * 
   * @param LinkedList of basic blocks.
   * 
   * @return void.
   */
  public void printBasicBlocks(LinkedList<BasicBlock> basicBlocks) {
    for (int i = 0; i < basicBlocks.size(); i++) {
      System.out.printf("Basic Block %d:\n", i + 1);
      System.out.println(basicBlocks.get(i));
    }
  }

  /**
   * Ensures that the table does not become misaligned. If one arraylist has more
   * or less elements than the rest, then it is misaligned and an error should be
   * thrown.
   * 
   * @return false is misaligned, true otherwise.
   */
  private boolean verifySize() {
    return RES_LIST.size() == OPERAND1_LIST.size() && OPERAND1_LIST.size() == OPERAND2_LIST.size()
        && OPERAND2_LIST.size() == OPERATOR_LIST.size();
  }

  /**
   * Concatenates an if statement to the toString() output.
   * 
   * Quadruple stores IF as follows:
   * 
   * RES_LIST: "goto " + LABEL OPERAND1_LIST: First operand of if conditional.
   * OPERAND2_LIST: Second operand of if conditional. OPERATOR_LIST: Relational
   * operator or logical operator of conditional.
   * 
   * @param output
   * @param i
   */
  private void printIfStatement(StringBuilder output, int i) {
    String op = OPERATOR_LIST.get(i).substring(2);
    output.append("if ");
    output.append(OPERAND1_LIST.get(i));
    output.append(" ");
    output.append(op);
    output.append(" ");
    output.append(OPERAND2_LIST.get(i));
    output.append(" ");
    output.append(RES_LIST.get(i));
    output.append("\n");
  }

  /**
   * Concatenates a non-void function to the toString() output.
   * 
   * Quadruple stores these as follows:
   * 
   * RES_LIST: temporary variable storing return value of function. OPERAND1_LIST:
   * function name with gf_ prefix. OPERAND2_LIST: number of arguments to
   * function. OPERATOR_LIST: "call" keyword for function.
   * 
   * @param output
   * @param i
   */
  private void printFunctionCall(StringBuilder output, int i) {
    output.append(RES_LIST.get(i));
    output.append(" = ");
    output.append(OPERATOR_LIST.get(i));
    output.append(" ");
    output.append(OPERAND1_LIST.get(i));
    output.append(",");
    output.append(OPERAND2_LIST.get(i));
    output.append("\n");
  }

  /**
   * Concatenates a void function to the toString() output.
   * 
   * Quadruple stores these as follows:
   * 
   * OPERAND1_LIST: function name with gf_ prefix. OPERAND2_LIST: number of
   * arguments to function. OPERATOR_LIST: "call" keyword for function.
   * 
   * @param output
   * @param i
   */
  private void printVoidFunctionCall(StringBuilder output, int i) {
    output.append(OPERATOR_LIST.get(i));
    output.append(" ");
    output.append(OPERAND1_LIST.get(i));
    output.append(",");
    output.append(OPERAND2_LIST.get(i));
    output.append("\n");
  }

  /**
   * Concatenates a cast to the toString() output.
   * 
   * Quadruple stores a cast statement as follows:
   * 
   * RES_LIST: temporary variable for storing the cast eval. OPERAND1_LIST: rvalue
   * to be casted. OPERATOR_LIST: operator that performs the cast (widen, narrow,
   * &).
   * 
   * @param output
   * @param i
   */
  private void printCast(StringBuilder output, int i) {
    output.append(RES_LIST.get(i));
    output.append(" = ");
    output.append(OPERATOR_LIST.get(i));
    output.append(" ");
    output.append(OPERAND1_LIST.get(i));
    output.append("\n");
  }

  /**
   * Concatenates a function declaration to the toString() output.
   * 
   * Quadruple stores a function declaration as follows:
   * 
   * RES_LIST: Name of function with gf_ prefix. OPERAND1_LIST: .fnStart
   * OPERAND2_LIST: local space needed for function.
   * 
   * @param output
   * @param i
   */
  private void printFunctionDeclaration(StringBuilder output, int i) {
    String fnLabel = RES_LIST.get(i).contains("gf_") ? (RES_LIST.get(i) + ":") : RES_LIST.get(i);
    output.append(fnLabel);
    output.append(" ");
    output.append(OPERAND1_LIST.get(i));
    output.append(" ");
    output.append(OPERAND2_LIST.get(i));
    output.append("\n");
  }

  /**
   * Concatenates a return statement to the toString() output.
   * 
   * Quadruple stores a return statement as follows:
   * 
   * OPERAND1_LIST: return value. OPERATOR_LIST: "return" keyword with data size
   * appended (e.g. return4, return1).
   * 
   * @param output
   * @param i
   */
  private void printReturn(StringBuilder output, int i) {
    output.append(OPERATOR_LIST.get(i));
    output.append(" ");
    output.append(OPERAND1_LIST.get(i));
    output.append("\n");
  }

  /**
   * Concatenates a global variable declaration to the toString() output.
   * 
   * Quadruple stores a global variable declaration as follows:
   * 
   * RES_LIST: address of global variable. OPERAND1_LIST: ".dw" or ".db"
   * OPERAND2_LIST: literal value of global.
   * 
   * @param output
   * @param i
   */
  private void printGlobalVariable(StringBuilder output, int i) {
    output.append(RES_LIST.get(i));
    output.append(": ");
    output.append(OPERAND1_LIST.get(i));
    output.append(" ");
    output.append(OPERAND2_LIST.get(i));
    output.append("\n");
  }

  /**
   * Concatenates a parameter for a function to the toString() output.
   * 
   * Quadruple stores the parameter as follows:
   * 
   * OPERAND1_LIST: parameter value. OPERATOR_LIST: "param" keyword with data size
   * appended (e.g. param4, param1).
   * 
   * @param output
   * @param i
   */
  private void printParameter(StringBuilder output, int i) {
    output.append(OPERATOR_LIST.get(i));
    output.append(" ");
    output.append(OPERAND1_LIST.get(i));
    output.append("\n");
  }

  /**
   * Adds a tab character to the output string builder object if necessary. Tab
   * characters help distinguish labels, function declarations, and global
   * variable declarations in the output.
   * 
   * @param output
   * @param i
   */
  private void indentOutput(StringBuilder output, int i) {
    if (RES_LIST.get(i).startsWith("gf")) {
      return;
    } else if (RES_LIST.get(i).startsWith("L")) {
      return;
    } else if (RES_LIST.get(i).startsWith("g") && OPERAND1_LIST.get(i).startsWith(".d")) {
      return;
    }
    output.append("\t");
  }

  /**
   * 
   */
  @Override
  public String toString() {
    StringBuilder output = new StringBuilder();
    for (int i = 0; i < RES_LIST.size(); i++) {
      String resVal = RES_LIST.get(i);
      String opVal = OPERATOR_LIST.get(i);
      String op1Val = OPERAND1_LIST.get(i);
      String op2Val = OPERAND2_LIST.get(i);

      this.indentOutput(output, i);

      // Print cast.
      if (opVal.equals("widen") || opVal.equals("narrow")) {
        this.printCast(output, i);
        continue;
      }

      // Print if statement.
      if (opVal.contains("if")) {
        this.printIfStatement(output, i);
        continue;
      }

      // If we're printing a void function we don't need a temporary variable.
      if (opVal.equals("call") && resVal.isEmpty()) {
        this.printVoidFunctionCall(output, i);
        continue;
      }

      // If we're printing a return value, do it here.
      if (opVal.contains("return")) {
        this.printReturn(output, i);
        continue;
      }

      // If we're printing a non-void function, it needs a temporary variable.
      if (op1Val.contains("gf_")) {
        this.printFunctionCall(output, i);
        continue;
      }

      // Print global variable.
      if (op1Val.contains(".d")) {
        this.printGlobalVariable(output, i);
        continue;
      }

      // Print function declaration.
      if (op1Val.equals(".fnStart")) {
        this.printFunctionDeclaration(output, i);
        continue;
      }

      // Print parameter.
      if (opVal.contains("param")) {
        this.printParameter(output, i);
        continue;
      }

      // Here, we print out all other things.
      output.append(resVal);
      output.append(" ");

      // Local array declaration.
      if (opVal.contains("setsize")) {
        output.append(opVal);
        output.append(" ");
        output.append(op1Val);
      }
      // Unary operator printing.
      else if (op2Val.isEmpty() && !opVal.equals("=") && !resVal.contains("L") && !resVal.contains(".fnEnd")
          && !resVal.contains(".db") && !resVal.contains(".dw")) {
        output.append("= ");
        output.append(opVal);
        output.append(" ");
        output.append(op1Val);
      } else if (op2Val.isEmpty()) {
        output.append(opVal);
        output.append(" ");
        output.append(op1Val);
      } else {
        output.append("= ");
        output.append(op1Val);
        output.append(" ");
        output.append(opVal);
        output.append(" ");
        output.append(op2Val);
      }

      output.append("\n");
    }

    /* Afterwards, we need to append each string literal. */
    for (StringEntry se : ICode.getTopAR().getStringTable()) {
      output.append(se.getCompilerID() + ": ");
      output.append(se.toString());
      output.append("\n");
    }

    return output.toString();
  }
}