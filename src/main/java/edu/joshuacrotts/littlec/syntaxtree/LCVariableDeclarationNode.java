package edu.joshuacrotts.littlec.syntaxtree;

import org.antlr.v4.runtime.ParserRuleContext;

import edu.joshuacrotts.littlec.icode.ICInhAttr;
import edu.joshuacrotts.littlec.icode.ICode;
import edu.joshuacrotts.littlec.main.LCErrorListener;
import edu.joshuacrotts.littlec.main.LCUtilities;
import edu.joshuacrotts.littlec.main.SymbolEntry;
import edu.joshuacrotts.littlec.main.SymbolTable;

public class LCVariableDeclarationNode extends LCSyntaxTree {

  /** Name of the variable for later reference. */
  private String id;

  /** Type of variable that we're declaring. */
  private String varType;

  /** The literal value that we're assigning. Null if no lit. */
  private Object literalValue;

  /**
   * Creates a node for variable declaration in the tree. This includes int x = 5;
   * int x;
   * 
   * @parse ctx - ParserRuleContext to use for passing to the error handler.
   * @param symbolTable  - table to check against for the existence of our
   *                     identifier.
   * @param id           - identifier of lvalue.
   * @param varType      - type of lvalue.
   * @param storageClass - storage class of variable.
   * @param literalValue - object either of type int, char, or String.
   */
  public LCVariableDeclarationNode(ParserRuleContext ctx, SymbolTable symbolTable, String id, String varType,
      String storageClass, Object literalValue) {
    super("DECL", "void", id + " " + "(" + varType + ")" + (literalValue != null ? " = " + literalValue : ""));
    this.id = id;
    this.varType = varType;
    this.literalValue = literalValue;

    // If we don't have the symbol in the current environment table (which defines
    // the current scope, then we're good to add it (we can shadow it). 
    if (!symbolTable.hasSymbolInCurrentEnvironment(this.id)) {

      // If the symbol DOES exist somewhere in the table (in other words, it's either
      // shadowing a variable or there's a function with the same name declared), then
      // we need to make sure it's not previously declared as a function.
      if (symbolTable.hasSymbol(this.id)) {
        String symbolEntry = symbolTable.getSymbolEntry(this.id).getType();
        if (symbolEntry.equals("FNDEF")) {
          LCErrorListener.syntaxError(ctx, this.id + " was previously declared as a function.");
          return;
        }
      }

      symbolTable.addSymbol(this.id, new SymbolEntry("VAR", varType, storageClass));
      return;
    } else {
      LCErrorListener.syntaxError(ctx, this.id + " has already been declared in this scope.");
    }
  }

  /**
   * It's not the cleanest solution but hey, it works.
   * 
   * I suspect a major refactoring is to come over winter break...
   */
  @Override
  public void genCode(ICInhAttr info) {
    if (super.isCalled)
      return;
    super.isCalled = true;

    // If we're in an array reference or declaration, we need to do something
    // different.
    if (this.varType.endsWith("]")) {
      String arrayType = LCUtilities.getArrayType(this.varType);
      int arraySize = LCUtilities.getArraySize(this.varType);
      int dataWidth = LCUtilities.getDataWidth(this.varType);

      // Add array to global scope.
      if (ICode.getARStackSize() == 1) {
        String gLabel = ICode.getTopAR().addGlobalVariable(id, 0);
        String gLabelDecl = ".dw ";
        String arrayInitSizeLabel = this.getICLabel(arrayType) + " 0#" + arraySize;
        ICode.quad.addLine(gLabel, gLabelDecl, Integer.toString(arraySize), "");

        // If we have a char literal, then we need to add its declaration.
        if (arrayType.equals("char") && this.literalValue != null) {
          String lit = this.literalValue.toString();
          lit = LCUtilities.escapeString(lit.substring(1, lit.length() - 1));
          String byteChars = LCUtilities.getByteString(lit);
          int remainingChars = arraySize - lit.length() - 1;
          ICode.quad.addLabel(byteChars);
          
          // If we have remaining chars, use the zero-padding operation.
          if (remainingChars > 0) 
            ICode.quad.addLabel(".db 0#" + remainingChars);
        } else {
          ICode.quad.addLabel(arrayInitSizeLabel);
        }

        info.ADDR = gLabel;
      }
      // Add array to local scope.
      else {
        String lLabel = ICode.getTopAR().addLocalArray(id, 0, dataWidth);
        int size = LCUtilities.getDataWidth(this.varType);
        ICode.quad.addLine(lLabel, Integer.toString(arraySize), "", "setsize" + size);
        info.ADDR = lLabel;
      }
    }
    /* Otherwise, we insert the value as normal. */
    else {
      int dataWidth = LCUtilities.getDataWidth(this.varType);
      String lit = this.literalValue == null ? "0" : this.literalValue.toString();

      // Add array to global scope.
      if (ICode.getARStackSize() == 1) {
        String gLabel = ICode.getTopAR().addGlobalVariable(id, dataWidth);
        String gLabelDecl = this.getICLabel(this.varType);
        ICode.quad.addLine(gLabel, gLabelDecl, lit.toString(), "");
        info.ADDR = gLabel;
      }
      // Add array to local scope.
      else {
        String lLabel = ICode.getTopAR().addLocalVariable(id, dataWidth);
        ICode.quad.addLine(lLabel, lit, "=");
        info.ADDR = lLabel;
      }
    }
  }
  
  /**
   * 
   * @param type
   * @return
   */
  private String getICLabel(String type) {
    switch (type) {
    case "int":
      return ".dw";
    case "float":
      return ".df";
    case "char":
      return ".db";
    }
    return null;
  }

  @Override
  public String toString() {
    return this.getType() + " " + this.getLabel() + " " + this.getInfo();
  }
}
