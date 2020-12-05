package edu.joshuacrotts.littlec.mipsgen;

import edu.joshuacrotts.littlec.icode.ICAddress;

public class CodeGeneration {

  /**
   * Generates the MIPS instruction for the supplied three-address-code
   * instruction.
   * 
   * @param progState - ProgState object.
   * @param res       - destination to store.
   * @param op1       - first operand.
   * @param op2       - second operand.
   * @param op        - operator.
   * 
   * @return String MIPS representation.
   */
  public static String genInstruction(MIPSFunction function, ProgState progState, String res, String op1, String op2,
      String op) {
    StringBuilder sb = new StringBuilder();
    if (op1.contains(".fn") || res.contains(".fn")) { // FN END OR START
      return sb.toString();
    } else if (op.contains("widen") || op.contains("narrow")) { // CAST
      sb.append(emitCast(progState, new ICAddress(res), new ICAddress(op1)));
    } else if (op.contains("setsize")) { // ARRAY DECL
      int dataSize = Integer.parseInt(op.substring(7));
      sb.append(emitArrayDecl(progState, new ICAddress(res, dataSize), new ICAddress(op1)));
    } else if (op.contains("stidx")) { // ARRAY STIDX
      sb.append(emitArrayStore(progState, new ICAddress(res), new ICAddress(op1), new ICAddress(op2)));
    } else if (op.contains("ldidx")) { // ARRAY LDIDX
      sb.append(emitArrayLoad(progState, new ICAddress(res), new ICAddress(op1), new ICAddress(op2)));
    } else if (op.contains("#")) { // ARRAY SIZE OF
      sb.append(emitSizeOf(progState, new ICAddress(res), new ICAddress(op1)));
    } else if (op.contains("param")) { // PARAM
      int size = Integer.parseInt(op.substring(5, 6));
      sb.append(emitParam(function, progState, new ICAddress(op1, size)));
    } else if (op.contains("call") && res.isEmpty()) { // VOID FUNCTION CALL
      sb.append(emitVoidFunctionCall(function, progState, op1, op2));
    } else if (op.contains("call")) { // NON-VOID FUNCTION CALL
      sb.append(emitNonVoidFunctionCall(function, progState, new ICAddress(res), op1, op2));
    } else if (op.equals("=")) { // ASN
      sb.append(emitAssignment(progState, new ICAddress(res), new ICAddress(op1)));
    } else if (op.contains("if")) { // IF
      sb.append(emitIf(progState, res, new ICAddress(op1), new ICAddress(op2), op));
    } else if (!op2.isEmpty()) { // BINOP
      sb.append(emitBinaryOp(progState, new ICAddress(res), new ICAddress(op1), new ICAddress(op2), op));
    } else if (op.contains("return") && !op1.isEmpty()) { // RETURN IN NON-VOID FUNCTION STATEMENT
      int size = Integer.parseInt(op.substring(6, 7));
      sb.append(emitReturn(function, progState, new ICAddress(op1, size)));
    } else if (op.contains("return")) { // RETURN IN VOID FUNCTION STATEMENT
      sb.append(emitReturn(function, progState));
    } else if (op2.isEmpty() && !op.isEmpty()) { // UNARYOP
      sb.append(emitUnaryOp(progState, new ICAddress(res), new ICAddress(op1), op));
    } else if (res.contains("goto") && op.isEmpty()) { // GOTO
      sb.append(emitLabel(progState, res));
    } else { // LBL
      sb.append(emitLabel(progState, res));
    }

    return sb.toString();
  }

  /**
   * Emits a cast operation. A cast in this case will simply move the values into
   * a register that's "wider", so to speak. I don't really think I got this one
   * to work well...
   * 
   * @param progState - ProgState object.
   * @param res       - value to store casted object in.
   * @param op1       - value to cast.
   * 
   * @return MIPS instructions resulted from cast.
   */
  public static String emitCast(ProgState progState, ICAddress res, ICAddress op1) {
    StringBuilder sb = new StringBuilder();
    boolean needToStore = false;

    // Load in the value to be cast.
    MIPSReg op1Reg = progState.getCurrReg(op1);
    if (op1Reg == null) {
      op1Reg = progState.getNextAvailableRegister();
      sb.append(MIPSInstruction.genLoad(getMIPSLoadOp(op1), op1Reg, progState.getCanonicalMIPS(op1)));
    }
    progState.copyVal(op1Reg, op1);

    // Now load the value that we want to store the cast in.
    MIPSReg resReg = progState.getCurrReg(res);
    if (resReg == null) {
      resReg = progState.getNextAvailableRegister();
      sb.append(MIPSInstruction.genLoad(getMIPSLoadOp(res), resReg, progState.getCanonicalMIPS(res)));
      needToStore = true;
    }
    progState.copyVal(resReg, res);

    // Move the src into dest.
    progState.copyVal(resReg, op1Reg);
    sb.append(MIPSInstruction.genMove(resReg, op1Reg));

    // Store the result in the destination.
    if (needToStore) {
      progState.copyVal(op1, resReg);
      sb.append(MIPSInstruction.genStore(getMIPSStoreOp(op1), resReg, progState.getCanonicalMIPS(res)));
    }

    progState.invalidate(op1Reg);
    progState.invalidate(resReg);

    return sb.toString();
  }

  /**
   * Emits a parameter. In MIPS, we have to push these onto the stack, then
   * immediately pop them off, storing the first four into the $a registers. This
   * step decrements four bytes from the stack, then stores the result on the
   * stack.
   * 
   * @param function  - MIPSFunction object.
   * @param progState - ProgState object.
   * @param param     - parameter to store.
   * 
   * @return MIPS representation of storing param on stack.
   */
  public static String emitParam(MIPSFunction function, ProgState progState, ICAddress param) {
    StringBuilder sb = new StringBuilder();

    // Load param into tmp
    MIPSReg paramReg = progState.getCurrReg(param);
    if (paramReg == null) {
      paramReg = progState.getNextAvailableRegister(null, null);
      sb.append("\t" + getMIPSLoadOp(param) + " " + paramReg + ", " + progState.getCanonicalMIPS(param) + "\n");
    }

    sb.append("\tsubu $sp, $sp, 4\n");
    sb.append("\tsw" + " " + paramReg + ", " + "0($sp)\n");
    function.currentParamCount++;

    return sb.toString();
  }


  /**
   * Calls a void function. The fist four parameters are popped off the stack and
   * inserted into the $a registers. The rest remain on the stack. Once the
   * function is complete, we remove the remaining registers by applying a byte
   * offset to the top of the stack.
   * 
   * @param function  - MIPSFunction object.
   * @param progState - ProgState object.
   * @param fnName    - name of function we're calling.
   * 
   * @return MIPS representation of called function.
   */
  public static String emitVoidFunctionCall(MIPSFunction function, ProgState progState, String fnName, String argCount) {
    StringBuilder sb = new StringBuilder();
    int fnArgCount = Integer.parseInt(argCount);
    
    int idx = 0;
    // Load the parameters off the stack and push them into their respective
    // registers.
    for (idx = 0; idx < fnArgCount && idx < 4; idx++) {
      MIPSReg R = progState.getNextAvailableRegister(null, null);
      MIPSReg a = MIPSReg.aReg(idx);
      progState.copyVal(R, a);
      sb.append("\tlw" + " " + R + ", " + "0($sp)\n");
      sb.append("\tmove" + " " + a + ", " + R + "\n");
      sb.append("\taddu $sp, $sp, 4\n");
    }

    // Call the function.
    sb.append("\tjal" + " " + fnName + "\n");

    // Remove the other parameters from the stack, if they exist.
    if (idx >= 4 && idx < function.currentParamCount) {
      sb.append("\taddu $sp, $sp, " + ((function.currentParamCount - 4) * 4) + "\n");
    }

    function.currentParamCount = 0;
    return sb.toString();
  }

  /**
   * Calls a non-void function. The fist four parameters are popped off the stack
   * and inserted into the $a registers. The rest remain on the stack. Once the
   * function is complete, we remove the remaining registers by applying a byte
   * offset to the top of the stack. The return value is moved from the $v0
   * register into wherever it should be.
   * 
   * @param function - MIPSFunction object.
   * @param progState - ProgState object.
   * @param fnName    - name of function we're calling.
   * 
   * @return MIPS representation of called function.
   */
  public static String emitNonVoidFunctionCall(MIPSFunction function, ProgState progState, ICAddress res, String fnName, String argCount) {
    StringBuilder sb = new StringBuilder();
    int fnArgCount = Integer.parseInt(argCount);
    
    int idx = 0;
    // Load the parameters off the stack and push them into their respective
    // registers.
    for (idx = 0; idx < fnArgCount && idx < 4; idx++) {
      MIPSReg R = progState.getNextAvailableRegister(null, null);
      MIPSReg a = MIPSReg.aReg(idx);
      progState.copyVal(R, a);
      sb.append("\tlw" + " " + R + ", " + "0($sp)\n");
      sb.append("\tmove" + " " + a + ", " + R + "\n");
      sb.append("\taddu $sp, $sp, 4\n");
    }

    // Call the function.
    sb.append("\tjal" + " " + fnName + "\n");

    // Remove the other parameters from the stack, if they exist.
    if (idx >= 4 && idx < function.currentParamCount) {
      sb.append("\taddu $sp, $sp, " + ((function.currentParamCount - 4) * 4) + "\n");
    }

    // Get a temporary register for the return value;
    MIPSReg retReg = progState.getCurrReg(res);
    boolean needToStore = false;
    if (retReg == null) {
      retReg = progState.getNextAvailableRegister(null, null);
      needToStore = true;
    }

    // Copy v0 into this register
    sb.append("\tmove" + " " + retReg + ", " + MIPSReg.vReg(0) + "\n");
    progState.copyVal(retReg, MIPSReg.vReg(0));

    // Now store the value in res ICAddress.
    if (needToStore) {
      sb.append("\t" + getMIPSStoreOp(res) + " " + retReg + ", " + progState.getCanonicalMIPS(res) + "\n");
    }

    function.currentParamCount = 0;
    return sb.toString();
  }

  /**
   * Emits a return statement for a void function. This is equivalent to just
   * jumping to the epilogue. We also reset the number of parameters used by the
   * MIPSFunction object.
   * 
   * @param function  - MIPSFunction object.
   * @param progState - ProgState object.
   * 
   * @return MIPS representation of return stmt.
   */
  public static String emitReturn(MIPSFunction function, ProgState progState) {
    StringBuilder sb = new StringBuilder();

    // Now branch to the epilogue.
    sb.append(MIPSInstruction.genBranch("xf_" + function.getFunctionBlock().getFunctionName()));

    function.currentParamCount = 0;
    return sb.toString();
  }

  /**
   * Emits a return statement for non-void functions. The return value is placed
   * into the $v0 register, and we immediately jump to the epilogue after that.
   * 
   * @param function  - MIPSFunction object.
   * @param progState - ProgState object.
   * 
   * @return MIPS representation of return stmt.
   */
  public static String emitReturn(MIPSFunction function, ProgState progState, ICAddress ret) {
    StringBuilder sb = new StringBuilder();

    // Generate load for return value.
    MIPSReg retReg = progState.getCurrReg(ret);
    if (retReg == null) {
      retReg = progState.getNextAvailableRegister();
      sb.append(MIPSInstruction.genLoad(getMIPSLoadOp(ret), retReg, progState.getCanonicalMIPS(ret)));
    }
    progState.copyVal(retReg, ret);

    // Move the value into the return register.
    progState.copyVal(MIPSReg.vReg(0), retReg);
    sb.append(MIPSInstruction.genMove(MIPSReg.vReg(0), retReg));

    // Now branch to the epilogue.
    sb.append(MIPSInstruction.genBranch("xf_" + function.getFunctionBlock().getFunctionName()));

    function.currentParamCount = 0;
    return sb.toString();
  }

  /**
   * Emits an array declaration. We load the address into a register, as well as
   * the size. Then, we grab the pointer to the 0th index, and set the size at
   * that index.
   * 
   * @param progState - ProgState object.
   * @param res       - local variable for array decl.
   * @param size      - size of array; must be a literal.
   * 
   * @return MIPS string representation of array declaration.
   */
  public static String emitArrayDecl(ProgState progState, ICAddress res, ICAddress size) {
    StringBuilder sb = new StringBuilder();

    // Load the address of the array if it exists (it WON'T, but just for the hell
    // of it.
    MIPSReg resReg = progState.getCurrReg(res);
    if (resReg == null) {
      resReg = progState.getNextAvailableRegister();
      sb.append(MIPSInstruction.genLoad("la", resReg, progState.getCanonicalMIPS(res)));
    }
    progState.copyVal(resReg, res);

    // Now, load the size of the array into a register.
    MIPSReg sizeReg = progState.getNextAvailableRegister();
    sb.append(MIPSInstruction.genLoad("li", sizeReg, progState.getCanonicalMIPS(size)));
    progState.copyVal(sizeReg, res);

    // Now, store the size in the first element of the array AT that array
    // 0($resReg).
    sb.append(MIPSInstruction.genStore("sw", sizeReg, "0(" + resReg + ")"));

    // Invalidate the registers we used.
    progState.invalidate(resReg);
    progState.invalidate(sizeReg);

    return sb.toString();
  }

  /**
   * Emits a store operation into an array.
   * 
   * @param progState - ProgState object.
   * @param res       - variable representing address of array.
   * @param val       - value to store in array.
   * @param idx       - index to store.
   * 
   * @return MIPS representation of store operation.
   */
  public static String emitArrayStore(ProgState progState, ICAddress res, ICAddress idx, ICAddress val) {
    StringBuilder sb = new StringBuilder();
    // Load the address of the array if it exists (it WON'T, but just for the hell
    // of it.
    MIPSReg resReg = progState.getCurrReg(res);
    if (resReg == null) {
      resReg = progState.getNextAvailableRegister();
      sb.append(MIPSInstruction.genLoad("lw", resReg, progState.getCanonicalMIPS(res)));
    }
    progState.copyVal(resReg, res);

    // Load the idx into a temp.
    MIPSReg idxReg = progState.getCurrReg(idx);
    if (idxReg == null) {
      idxReg = progState.getNextAvailableRegister();
      sb.append(MIPSInstruction.genLoad(getMIPSLoadOp(idx), idxReg, progState.getCanonicalMIPS(idx)));
    }
    progState.copyVal(idxReg, idx);

    // Load the value into an address.
    MIPSReg valReg = progState.getCurrReg(val);
    if (valReg == null) {
      valReg = progState.getNextAvailableRegister();
      sb.append(MIPSInstruction.genLoad(getMIPSLoadOp(val), valReg, progState.getCanonicalMIPS(val)));
    }
    progState.copyVal(valReg, val);

    // Create a temp idx register.
    MIPSReg modIdxReg = progState.getNextAvailableRegister(null, null);
    progState.copyVal(modIdxReg, idxReg);
    sb.append(MIPSInstruction.genMove(modIdxReg, idxReg));
    idxReg = modIdxReg;

    // If the array is an int array, then we apply a 4byte offset.
    if (res.getWidth() == 4) {
      sb.append(MIPSInstruction.genBinaryOp("sll", "" + idxReg, "" + idxReg, "" + 2));
    }

    // Apply idx offset.
    sb.append(MIPSInstruction.genBinaryOp("addu", "" + idxReg, "" + resReg, "" + idxReg));

    // Store value in array.
    sb.append(MIPSInstruction.genStore(getMIPSStoreOp(res), valReg, "4(" + idxReg + ")"));

    progState.invalidate(modIdxReg);
    progState.invalidate(resReg);
    progState.invalidate(idxReg);
    progState.invalidate(valReg);

    return sb.toString();
  }

  /**
   * Emits a load operation into an array. The value pulled from the array is
   * stored into res.
   * 
   * @param progState - ProgState object.
   * @param res       - location to store value loaded from array.
   * @param arr       - variable representing address of array.
   * @param idx       - index to load from.
   * 
   * @return MIPS representation of store operation.
   */
  public static String emitArrayLoad(ProgState progState, ICAddress res, ICAddress arr, ICAddress idx) {
    StringBuilder sb = new StringBuilder();

    // Load the destination.
    MIPSReg resReg = progState.getCurrReg(res);
    if (resReg == null) {
      resReg = progState.getNextAvailableRegister();
      sb.append(MIPSInstruction.genLoad("lw", resReg, progState.getCanonicalMIPS(res)));
    }
    progState.copyVal(resReg, res);

    // Load the array.
    MIPSReg arrReg = progState.getCurrReg(arr);
    if (arrReg == null) {
      arrReg = progState.getNextAvailableRegister();
      sb.append(MIPSInstruction.genLoad("lw", arrReg, progState.getCanonicalMIPS(arr)));
    }
    progState.copyVal(arrReg, arr);

    // Now load the idx reg.
    MIPSReg idxReg = progState.getCurrReg(idx);
    if (idxReg == null) {
      idxReg = progState.getNextAvailableRegister();
      sb.append(MIPSInstruction.genLoad(getMIPSLoadOp(idx), idxReg, progState.getCanonicalMIPS(idx)));
    }
    progState.copyVal(arrReg, arr);

    // Create a temp idx register.
    MIPSReg modIdxReg = progState.getNextAvailableRegister();
    progState.copyVal(modIdxReg, idxReg);
    sb.append(MIPSInstruction.genMove(modIdxReg, idxReg));
    idxReg = modIdxReg;

    // If the array is an int array, then we apply a 4byte offset.
    if (res.getWidth() == 4) {
      sb.append(MIPSInstruction.genBinaryOp("sll", "" + idxReg, "" + idxReg, "2"));
    }

    // Add the idx offset.
    sb.append(MIPSInstruction.genBinaryOp("addu", "" + idxReg, "" + arrReg, "" + idxReg));

    // Load the value into the register.
    sb.append(MIPSInstruction.genLoad(getMIPSLoadOp(res), resReg, "4(" + idxReg + ")"));

    // Store the value into the dest memory.
    sb.append(MIPSInstruction.genLoad("sw", resReg, progState.getCanonicalMIPS(res)));

    progState.copyVal(res, resReg);

    progState.invalidate(modIdxReg);
    progState.invalidate(resReg);
    progState.invalidate(arrReg);
    progState.invalidate(idxReg);

    return sb.toString();
  }

  /**
   * Emits an if conditional - can be a while or for loop as well, or just a
   * logical operator.
   * 
   * @param progState - ProgState object.
   * @param res       - location to jump to if condition is true.
   * @param op1       - first operand of if.
   * @param op2       - second operand of if.
   * @param op        - logical comparison operator.
   * 
   * @return MIPS string representation of if.
   */
  public static String emitIf(ProgState progState, String res, ICAddress op1, ICAddress op2, String op) {
    StringBuilder sb = new StringBuilder();

    String logOp = op.substring(2); // ifOP
    String jmpLabel = res.substring(5); // goto L

    // Get the registers if they exist. Otherwise, we use temporary ones.
    MIPSReg op1Reg = progState.getCurrReg(op1);
    MIPSReg op2Reg = progState.getCurrReg(op2);

    String cOp1 = progState.getCanonicalMIPS(op1);
    String cOp2 = progState.getCanonicalMIPS(op2);

    // Load the op1 register.
    if (op1Reg == null) {
      op1Reg = progState.getNextAvailableRegister();
      sb.append(MIPSInstruction.genLoad(getMIPSLoadOp(op1), op1Reg, cOp1));
    }
    progState.copyVal(op1Reg, op1);

    // Load the op2 register.
    if (op2Reg == null) {
      op2Reg = progState.getNextAvailableRegister();
      sb.append(MIPSInstruction.genLoad(getMIPSLoadOp(op2), op2Reg, cOp2));
    }
    progState.copyVal(op2Reg, op2);

    // Emit the stmt.
    sb.append(MIPSInstruction.genBinaryOp(getMIPSLogicalOp(logOp), "" + op1Reg, "" + op2Reg, jmpLabel));

    progState.invalidate(op1Reg);
    progState.invalidate(op2Reg);

    return sb.toString();
  }

  /**
   * Emits a binary operator.
   * 
   * @param progState - ProgState object.
   * @param res       - location to store value of binary op.
   * @param op1       - first operand of binop.
   * @param op2       - second operand of binop.
   * @param op        - binary operator to use.
   * 
   * @return MIPS string representation of if.
   */
  public static String emitBinaryOp(ProgState progState, ICAddress res, ICAddress op1, ICAddress op2, String op) {
    StringBuilder sb = new StringBuilder();
    boolean needToStore = false;

    // If the operands are literals, we need to fix their widths.
    if (op1.isLiteral()) {
      op1.setWidth(res.getWidth());
    }

    if (op2.isLiteral()) {
      op2.setWidth(res.getWidth());
    }

    // Load the res register.
    MIPSReg resReg = progState.getCurrReg(res);
    String cRes = progState.getCanonicalMIPS(res);
    if (resReg == null) {
      resReg = progState.getNextAvailableRegister();
      sb.append(MIPSInstruction.genLoad(getMIPSLoadOp(res), resReg, cRes));
      needToStore = true;
    }
    progState.copyVal(resReg, res);

    // Load the op1 register.
    MIPSReg op1Reg = progState.getCurrReg(op1);
    String cOp1 = progState.getCanonicalMIPS(op1);
    if (op1Reg == null) {
      op1Reg = progState.getNextAvailableRegister();
      sb.append(MIPSInstruction.genLoad(getMIPSLoadOp(op1), op1Reg, cOp1));
    }
    progState.copyVal(op1Reg, op1);

    // Load the op2 register.
    MIPSReg op2Reg = progState.getCurrReg(op2);
    String cOp2 = progState.getCanonicalMIPS(op2);
    if (op2Reg == null) {
      op2Reg = progState.getNextAvailableRegister();
      sb.append(MIPSInstruction.genLoad(getMIPSLoadOp(op2), op2Reg, cOp2));
    }
    progState.copyVal(op2Reg, op2);

    // Perform the binary op.
    sb.append(MIPSInstruction.genBinaryOp(getMIPSBinaryOp(op), "" + resReg, "" + op1Reg, "" + op2Reg));

    // Store the result if we loaded in a new temporary for the destination.
    if (needToStore) {
      sb.append(MIPSInstruction.genStore(getMIPSStoreOp(res), resReg, cRes));
      progState.copyVal(res, resReg);
    }

    // Invalidate the registers.
    progState.invalidate(resReg);
    progState.invalidate(op1Reg);
    progState.invalidate(op2Reg);

    return sb.toString();
  }

  /**
   * Emits a size of operator. Since we always use array references, we first get
   * the address of the array, then load the 0th index value at the pointer.
   * 
   * @param progState - ProgState object.
   * @param res       - location to store size of array.
   * @param src       - address of array.
   * 
   * @return MIPS representation of size of.
   */
  public static String emitSizeOf(ProgState progState, ICAddress res, ICAddress src) {
    StringBuilder sb = new StringBuilder();

    // Load the res register.
    MIPSReg resReg = progState.getCurrReg(res);
    String cRes = progState.getCanonicalMIPS(res);
    if (resReg == null) {
      resReg = progState.getNextAvailableRegister();
      sb.append(MIPSInstruction.genLoad("lw", resReg, cRes));
    }
    progState.copyVal(resReg, res);

    // Load the source reg.
    MIPSReg srcReg = progState.getCurrReg(src);
    String cSrc = progState.getCanonicalMIPS(src);
    if (srcReg == null) {
      srcReg = progState.getNextAvailableRegister();
      sb.append(MIPSInstruction.genLoad("lw", srcReg, cSrc));
    }
    progState.copyVal(srcReg, src);

    // Perform the sizeof op.
    sb.append(MIPSInstruction.genLoad("lw", resReg, "0(" + srcReg + ")"));

    // Store result.
    sb.append(MIPSInstruction.genStore("sw", resReg, progState.getCanonicalMIPS(res)));
    progState.copyVal(res, resReg);

    progState.invalidate(resReg);
    progState.invalidate(srcReg);

    return sb.toString();
  }

  /**
   * Emits a unary operator. This excludes the size of operator which has its own
   * method.
   * 
   * @param progState - ProgState object.
   * @param res       - location to store value of op.
   * @param op1       - operand.
   * @param op        - operator to perform.
   * 
   * @return MIPS representation.
   */
  public static String emitUnaryOp(ProgState progState, ICAddress res, ICAddress op1, String op) {
    StringBuilder sb = new StringBuilder();
    boolean needToStore = false;

    // Check to make sure that if the operand is a literal,
    // it shares the width of the dest.
    if (op1.isLiteral()) {
      op1.setWidth(res.getWidth());
    }

    // Load the res register.
    MIPSReg resReg = progState.getCurrReg(res);
    String cRes = progState.getCanonicalMIPS(res);
    if (resReg == null) {
      resReg = progState.getNextAvailableRegister();
      sb.append(MIPSInstruction.genLoad(getMIPSLoadOp(res), resReg, cRes));
      needToStore = true;
    }
    progState.copyVal(resReg, res);

    // Load the op1 register.
    MIPSReg op1Reg = progState.getCurrReg(op1);
    String cOp1 = progState.getCanonicalMIPS(op1);
    if (op1Reg == null) {
      op1Reg = progState.getNextAvailableRegister();
      sb.append(MIPSInstruction.genLoad(getMIPSLoadOp(op1), op1Reg, cOp1));
    }
    progState.copyVal(op1Reg, op1);

    // Perform the unary op.
    sb.append(MIPSInstruction.genUnaryOp(getMIPSUnaryOp(op), "" + resReg, "" + op1Reg));

    // Store the result if we loaded in a new temporary for the destination.
    if (needToStore) {
      progState.copyVal(res, resReg);
      sb.append(MIPSInstruction.genStore("sw", resReg, cRes));
    }

    // Invalidate the registers.
    progState.invalidate(resReg);
    progState.invalidate(op1Reg);

    return sb.toString();
  }

  /**
   * Emits an assignment operator.
   * 
   * @param progState - ProgState object.
   * @param res       - location to store src.
   * @param src       - value to copy into res.
   * 
   * @return MIPS string representation of assignment.
   */
  public static String emitAssignment(ProgState progState, ICAddress res, ICAddress src) {
    StringBuilder sb = new StringBuilder();
    boolean needToStore = false;

    // Check to set the width of the src if it's a literal to match
    // the result.
    if (src.isLiteral()) {
      src.setWidth(res.getWidth());
    }

    // Load the res register.
    MIPSReg resReg = progState.getCurrReg(res);
    String cRes = progState.getCanonicalMIPS(res);
    if (resReg == null) {
      resReg = progState.getNextAvailableRegister();
      sb.append(MIPSInstruction.genLoad(getMIPSLoadOp(res), resReg, cRes));
      needToStore = true;
    }
    progState.copyVal(resReg, res);

    // Load the source register.
    MIPSReg srcReg = progState.getCurrReg(src);
    String cSrc = progState.getCanonicalMIPS(src);
    if (srcReg == null) {
      srcReg = progState.getNextAvailableRegister();
      sb.append(MIPSInstruction.genLoad(getMIPSLoadOp(src), srcReg, cSrc));
      needToStore = true;
    }
    progState.copyVal(srcReg, src);

    // Move src into dest.
    sb.append(MIPSInstruction.genMove(resReg, srcReg));
    progState.copyVal(resReg, srcReg);

    // We can't store a register in a register so make sure
    // we're not doing that.
    if (needToStore && !resReg.toString().equals(cRes)) {
      sb.append(MIPSInstruction.genStore(getMIPSStoreOp(src), resReg, cRes));
      progState.copyVal(src, srcReg);
    }

    // Invalidate the registers.
    progState.invalidate(resReg);
    progState.invalidate(srcReg);

    return sb.toString();
  }

  /**
   * Emits a label. This can be in the form of a goto or just a plain destination
   * label. It handles both.
   * 
   * @param progState - ProgState object.
   * @param label     - label in the form of "goto LX" or "LX:".
   * 
   * @return MIPS string representation of label.
   */
  public static String emitLabel(ProgState progState, String label) {
    StringBuilder sb = new StringBuilder();
    
    // If the label is a goto dest label then print that out.
    if (label.contains("goto")) {
      String jmpLabel = label.substring(5);
      sb.append(MIPSInstruction.genJump(jmpLabel));
    } else {
      // Otherwise, just print the label.
      sb.append(MIPSInstruction.genLabel(label));
    }
    return sb.toString();
  }

  /**
   * Returns the appropriate store operation for the provided ICAddress.
   * 
   * @param ICAddress to use.
   * 
   * @throws IllegalArgumentException if width is not 4 or 1.
   * 
   * @return "sw" if width == 4, "sb" if width == 1.
   */
  private static String getMIPSStoreOp(ICAddress src) {
    switch (src.getWidth()) {
    case 4:
      return "sw";
    case 1:
      return "sb";
    default:
      throw new IllegalArgumentException("width " + src.getWidth() + " is invalid.");
    }
  }

  /**
   * Returns the appropriate load operator for the given ICAddress.
   * 
   * @param ICAddress to use.
   * 
   * @throws IllegalArgumentException if src is not a literal, string, array,
   *         or does not have width of 4 or 1.
   * 
   * @return "sw" if width == 4, "sb" if width == 1, "la" if string or array, "li"
   *         for lit.
   */
  private static String getMIPSLoadOp(ICAddress src) {
    // If we're a string or an array, we load the address of the array.
    // Literals use "li".
    if (src.isLiteral()) {
      return "li";
    } else if (src.isString() || src.isArray() || src.getWidth() == 0) {
      return "la";
    }

    // Otherwise, we use lw or lb.
    switch (src.getWidth()) {
    case 4:
      return "lw";
    case 1:
      return "lb";
    default:
      throw new IllegalArgumentException("width " + src.getWidth() + " is invalid.");
    }
  }

  /**
   * Returns the MIPS binary operator for the supplied 3AC binary op.
   * 
   * @param op - 3AC binary op.
   * 
   * @throws IllegalArgumentException if binary operator is invalid.
   * 
   * @return MIPS representation of binary op.
   */
  private static String getMIPSBinaryOp(String op) {
    switch (op) {
    case "+":
      return "addu";
    case "-":
      return "subu";
    case "*":
      return "mul";
    case "/":
      return "div";
    case "&&":
      return "and";
    case "||":
      return "or";
    case "%":
      return "rem";
    default:
      throw new IllegalArgumentException("Invalid MIPS binary operator " + op);
    }
  }

  /**
   * Returns the MIPS unary operator for the supplied 3AC unary op.
   * 
   * @param op - 3AC unary op.
   * 
   * @throws IllegalArgumentException is operator is invalid.
   * 
   * @return MIPS representation of unary op.
   */
  private static String getMIPSUnaryOp(String op) {
    switch (op) {
    case "&":
      return "move";
    case "-":
    case "!":
      return "negu";
    default:
      throw new IllegalArgumentException("Invalid MIPS unary operator " + op);
    }
  }

  /**
   * Gets the MIPS logical comparison operator for the supplied 3AC logicop.
   * 
   * @param logOp - String logop.
   * 
   * @throws IllegalArgumentException if logical operator is invalid.
   * 
   * @return MIPS logical op.
   */
  private static String getMIPSLogicalOp(String logOp) {
    switch (logOp) {
    case "<=":
      return "ble";
    case "<":
      return "blt";
    case ">=":
      return "bge";
    case ">":
      return "bgt";
    case "==":
      return "beq";
    case "!=":
      return "bne";
    default:
      throw new IllegalArgumentException("Invalid MIPS logical operator " + logOp);
    }
  }
}
