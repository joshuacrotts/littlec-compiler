package edu.joshuacrotts.littlec.syntaxtree;

import org.antlr.v4.runtime.ParserRuleContext;

import edu.joshuacrotts.littlec.icode.ICInhAttr;
import edu.joshuacrotts.littlec.icode.ICode;
import edu.joshuacrotts.littlec.main.SymbolTable;

public class LCVariableIdentifierNode extends LCSyntaxTree {

  /** 
   * Name of the variable for later reference. 
   */
  private String id;

  /**
   * Creates a variable identifier node. These are just references to a variable,
   * for instance, if it's used as a parameter, or just declared without a literal
   * value.
   * 
   * @param ctx
   * @param symbolTable
   * @param id
   * @param varType
   */
  public LCVariableIdentifierNode(ParserRuleContext ctx, SymbolTable symbolTable, String id, String varType) {
    super("VAR", varType, id);
    this.id = id;
  }

  /**
   * 
   */
  @Override
  public void genCode(ICInhAttr e) {
    if (super.isCalled)
      return;
    super.isCalled = true;

    // Check if it's a parameter first, then try for a local var,
    // then finally check for globals.
    if (ICode.getTopAR().getParameterVariable(this.id) != null) {
      e.ADDR = ICode.getTopAR().getParameterVariable(this.id);
    } else if (ICode.getTopAR().getLocalVariable(this.id) != null) {
      e.ADDR = ICode.getTopAR().getLocalVariable(this.id);
    } else if (ICode.getTopAR().getGlobalVariable(this.id) != null) {
      e.ADDR = ICode.getTopAR().getGlobalVariable(this.id);
    } else {
      throw new RuntimeException(id + "Internal compiler error - Invalid variable ID location.");
    }
  }

  @Override
  public String toString() {
    return this.getType() + " ID " + this.getInfo();
  }
}
