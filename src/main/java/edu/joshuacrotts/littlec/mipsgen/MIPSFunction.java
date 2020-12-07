package edu.joshuacrotts.littlec.mipsgen;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import edu.joshuacrotts.littlec.icode.BasicBlock;
import edu.joshuacrotts.littlec.icode.FunctionBlock;
import edu.joshuacrotts.littlec.icode.ICAddress;
import edu.joshuacrotts.littlec.main.LCUtilities;

/**
 * A class for generating and managing MIPS code for a single function. The
 * provided methods keeps track of how much memory has been set aside in the
 * stack frame for temporary variables and allocates new space on demand. The
 * rest of the implemention is up to you!
 * 
 * @author Joshua Crotts
 */
public class MIPSFunction {

  /**
   * Number of parameters that the function being called uses.
   */
  protected int numParams = 0;

  /**
   * Current number of parameters being used.
   */
  protected int currentParamCount = 0;

  /**
   * ProgState object.
   */
  private final ProgState progState;

  /**
   * FunctionBlock object. This object is a collection of 3AC instructions for
   * this fn.
   */
  private final FunctionBlock functionBlock;

  /**
   * Track which temp variables have been allocated memory, and where they are
   * (offsets). Note that all temp variables are 4 bytes long.
   */
  private final HashMap<String, Integer> tempVarLoc;

  /**
   * Track which local variables have been allocated memory, and where they are
   * (offsets).
   */
  private final HashMap<String, Integer> localVarLoc;

  /**
   * Boolean to determine if this function calls another function. If not, we can
   * optimize $ra to just store that return address.
   */
  private boolean isCallingOtherFunction = false;

  /**
   * How much memory has been allocated for local variables? Grow as needed.
   */
  private int localVarSize;

  /**
   * How much memory has been allocated for temp variables? Grow as needed.
   */
  private int tempVarSize;

  /**
   * Maximum number of $a (argument) registers we need to save in this block.
   */
  private int maxArgRegSize;

  /**
   * The size of the stack allocated for this function.
   */
  private int stackSpace;

  /**
   * Return address for this function.
   */
  private int returnAddress;

  /**
   * Previous function pointer stored in the stack.
   */
  private int previousFunctionPointer;

  /**
   * Value of s7 register saved on the stack.
   */
  private int s7Register;

  public MIPSFunction(FunctionBlock block) {
    this.currentParamCount = 0;
    this.tempVarSize = 0;
    this.localVarSize = 0;
    this.tempVarLoc = new HashMap<>();
    this.localVarLoc = new HashMap<>();
    this.functionBlock = block;
    this.initSizes();

    // Get the number of parameters needed and the size of local vars.
    // The invariant is that this is *always* the first line in the FB.
    this.progState = new ProgState(this, this.maxArgRegSize, this.localVarSize);
  }

  /**
   * Generates the MIPS instructions for this function.
   * 
   * @param void.
   * 
   * @return String representation of instructions.
   */
  public String genMIPS() {
    // The function name is the first element in the first row of the "instruction
    // list."
    String fnName = this.functionBlock.getFunctionName();
    StringBuilder sb = new StringBuilder();

    // Generate the three pieces individually so we can back-patch.
    String prologue = "";
    String epilogue = "";
    StringBuilder fnBody = new StringBuilder();

    // Generate the function body.
    LinkedList<BasicBlock> basicBlocks = this.functionBlock.generateFunctionBasicBlocks();
    for (int i = 0; i < basicBlocks.size(); i++) {

      // Build the code for the current basic block.
      BasicBlock bb = basicBlocks.get(i);
      for (int j = 0; j < bb.getNumberOfInstructions(); j++) {
        LinkedList<String> tac = bb.getCurrentInstruction(j);
        fnBody.append(CodeGeneration.genInstruction(this, progState, tac.get(0), tac.get(1), tac.get(2), tac.get(3)));
      }
      progState.clearTempRegisters();
    }

    // Generate the prologue and epilogue; we back-patch the prologue.
    prologue = this.genPrologue(fnName);
    epilogue = this.genEpilogue(fnName);

    // Append the prologue, function body, and epilogue to the main SB.
    sb.append(prologue);
    sb.append(fnBody);
    sb.append(epilogue);

    return sb.toString();
  }

  /**
   * Adds a temporary variable to the lookup table.
   * 
   * @param name
   * 
   * @return
   */
  public int getTVarLoc(ICAddress name) {
    Integer loc = tempVarLoc.get(name.getName());
    if (loc == null) {
      tempVarSize += 4;
      loc = tempVarSize;
      tempVarLoc.put(name.getName(), loc);
    }
    return loc;
  }

  /**
   * Adds a local variable to the lookup table.
   * 
   * @param name
   * 
   * @return
   */
  public int getLVarLoc(ICAddress name) {
    Integer loc = localVarLoc.get(name.getName());
    if (loc == null) {
      localVarSize += 4;
      loc = localVarSize;
      localVarLoc.put(name.getName(), loc);
    }
    return loc;
  }

  /**
   * Generates the function prologue for the function name provided. As of now,
   * the stack size and other offsets are computed in the initSize first pass
   * through the function block, and it works, but arguments still need to be
   * calculated.
   * 
   * @param fnName - function name without gf_ prefix.
   * 
   * @return String representation of instructions.
   */
  private String genPrologue(String fnName) {
    // We can optimize the $ra register by omitting it if this function does
    // *not* call another function.
    int calleeSavedSpace = this.isCallingOtherFunction ? 12 : 8;

    // SP saves necessary $s registers (min of 4 and however many we use), the # of
    // temp vars generated in 3AC, and the local var size, alongside the callee
    // saved space.
    this.stackSpace = Math.min(4, this.maxArgRegSize) * 4 + this.tempVarSize
        + LCUtilities.getNextAddress(this.localVarSize) + calleeSavedSpace;

    this.returnAddress = this.isCallingOtherFunction ? stackSpace - 4 : stackSpace;
    this.previousFunctionPointer = this.returnAddress - 4;
    this.s7Register = this.previousFunctionPointer - 4;

    // SP must be aligned on 8-byte boundaries.
    this.stackSpace = LCUtilities.getNextMIPSAddress(this.stackSpace);

    StringBuilder sb = new StringBuilder();
    // Append the prologue starting label.
    sb.append("gf_");
    sb.append(fnName);
    sb.append(":\n");
    sb.append("\t.globl");
    sb.append(" ");
    sb.append("gf_");
    sb.append(fnName);
    sb.append("\n");

    sb.append("\tsubu $sp, $sp, " + this.stackSpace).append("\n");
    if (this.isCallingOtherFunction) {
      sb.append("\tsw $ra, " + this.returnAddress + "($sp)").append("\n");
    }
    sb.append("\tsw $fp, " + this.previousFunctionPointer + "($sp)").append("\n");
    sb.append("\tsw $s7, " + this.s7Register + "($sp)").append("\n");
    sb.append(this.saveArgRegisters());
    sb.append("\taddiu $fp, $sp, " + (this.s7Register - (this.maxArgRegSize * 4))).append("\n");
    sb.append("\taddiu $s7, $sp, " + this.stackSpace).append("\n");

    return sb.toString();
  }

  /**
   * Generates the epilogue for the function name provided.
   * 
   * @param fnName - function name without gf_ prefix.
   * 
   * @return String representation of instructions.
   */
  private String genEpilogue(String fnName) {
    StringBuilder sb = new StringBuilder();
    // Append the epilogue starting label.
    sb.append("xf_");
    sb.append(fnName);
    sb.append(":\n");

    sb.append(this.loadArgRegisters());
    sb.append("\tlw $s7, " + this.s7Register + "($sp)").append("\n");
    sb.append("\tlw $fp, " + this.previousFunctionPointer + "($sp)").append("\n");
    if (this.isCallingOtherFunction) {
      sb.append("\tlw $ra, " + this.returnAddress + "($sp)").append("\n");
    }
    sb.append("\taddiu $sp, $sp, " + this.stackSpace).append("\n");
    sb.append("\tjr $ra").append("\n");

    return sb.toString();
  }

  /**
   * Saves all necessary $s0 registers into memory. Then, all argument registers
   * needed $a0-$an are moved into $s0-$sn. This is called during the function
   * prologue.
   * 
   * @param void.
   * 
   * @return String representation of MIPS instructions.
   */
  private String saveArgRegisters() {
    // If we don't use any parameters then there's nothing to save, even
    // if we call functions.
    StringBuilder sb = new StringBuilder();
    int offset = this.s7Register - (this.maxArgRegSize * MIPSReg.NUM_AREG);
    for (int i = 0; i < this.maxArgRegSize && i < MIPSReg.NUM_AREG; i++, offset += MIPSReg.NUM_AREG) {
      // Store the word into memory.
      sb.append("\tsw");
      sb.append(" $s");
      sb.append(i);
      sb.append(", ");
      sb.append(offset);
      sb.append("($sp)");
      sb.append("\n");

      // Move ax into sx
      sb.append("\tmove");
      sb.append(" $s");
      sb.append(i);
      sb.append(", $a");
      sb.append(i);
      sb.append("\n");

      MIPSReg sReg = MIPSReg.sReg(i);
      MIPSReg aReg = MIPSReg.aReg(i);
      this.progState.copyVal(sReg, aReg);
    }

    return sb.toString();
  }

  /**
   * Loads the data from memory back into the $s0 registers. This is called during
   * the function epilogue.
   * 
   * @param void.
   * 
   * @return String representation of MIPS instructions.
   */
  private String loadArgRegisters() {
    StringBuilder sb = new StringBuilder();
    int offset = this.s7Register - (this.maxArgRegSize * MIPSReg.NUM_AREG);
    for (int i = 0; i < this.maxArgRegSize && i < MIPSReg.NUM_AREG; i++, offset += MIPSReg.NUM_AREG) {
      // Load the word from memory.
      sb.append("\tlw");
      sb.append(" $s");
      sb.append(i);
      sb.append(", ");
      sb.append(offset);
      sb.append("($sp)");
      sb.append("\n");
    }

    return sb.toString();
  }

  /**
   * Performs an initial pass through the function to set temporary variable sizes
   * and determine the max number of arguments used (when we may or may not need
   * to save $a registers).
   * 
   * @param void.
   * 
   * @return void.
   */
  private void initSizes() {
    this.localVarSize = LCUtilities
        .getNextAddress(Integer.parseInt(this.functionBlock.getInstructions().get(0).get(2)));
    for (int i = 0; i < this.functionBlock.getInstructions().size(); i++) {
      LinkedList<String> currInst = this.functionBlock.getInstructions().get(i);
      for (int j = 0; j < currInst.size(); j++) {
        String s = currInst.get(j);

        if (s.startsWith("t")) {
          this.getTVarLoc(new ICAddress(s));
        }

        // If the address starts with an l, we know it's a local.
        // If the function calls another function, we can't optimize $ra.
        // However, if we are on a 'call', it means we're in the "op" section
        // of the quad. op2 stores the number of params passed (this is our
        // invariant).
        if (s.equals("call")) {
          this.maxArgRegSize = Math.max(this.maxArgRegSize, Integer.parseInt(currInst.get(j - 1)));
          this.isCallingOtherFunction = true;
        }
      }
    }
  }

  public FunctionBlock getFunctionBlock() {
    return this.functionBlock;
  }

  public ProgState getProgState() {
    return this.progState;
  }
}
