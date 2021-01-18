package edu.joshuacrotts.littlec.syntaxtree;

import org.antlr.v4.runtime.ParserRuleContext;

import edu.joshuacrotts.littlec.icode.ICInhAttr;
import edu.joshuacrotts.littlec.icode.ICode;
import edu.joshuacrotts.littlec.main.LCUtilities;
import edu.joshuacrotts.littlec.main.SymbolTable;

public class LCPrePostOperatorNode extends LCSyntaxTree {

  /**
   * Creates a node with the relevant pre/post operator, and the lvalue it is used
   * on.
   * 
   * Child 1 is one of the four possibilities PRE-INC, PRE-DEC, POST-INC,
   * POST-DEC.
   * 
   * Child 2 is the l-value being incremented or decremented.
   * 
   * @param symbolTable
   * @param type
   * @param lvarType
   * @param lvar
   */
  public LCPrePostOperatorNode(ParserRuleContext ctx, SymbolTable symbolTable, String type, String lvarType,
      LCSyntaxTree lvar) {
    super(type, lvarType);

    // One child for the lvalue being incremented or decremented.
    super.addChild(lvar);
  }

  /**
   * 
   */
  @Override
  public void genCode(ICInhAttr e) {
    if (super.isCalled)
      return;
    super.isCalled = true;
    String op = this.getPrePostOp(this.getLabel());

    // Get the width of the l-address type and
    // generate a temp variable if necessary.
    int dataWidth = LCUtilities.getDataWidth(this.getType());

    // Generate the lvalue IC.
    ICInhAttr e1 = new ICInhAttr();
    this.getChildren().get(0).genCode(e1);

    // If we have a post operator, then we generate a temporary variable.
    if (this.getLabel().startsWith("POST")) {
      String tempVar = ICode.getTopAR().addTemporaryVariable(dataWidth);
      ICode.quad.addLine(tempVar, e1.ADDR, "=");
      ICode.quad.addLine(e1.ADDR, e1.ADDR, "1", op);
      e.ADDR = tempVar;
    } else {
      ICode.quad.addLine(e1.ADDR, e1.ADDR, "1", op);
      e.ADDR = e1.ADDR;
    }
    
    // If our l-value is an array, we need to save the changed value.
    if (this.getChildren().get(0) instanceof LCArrayIndexNode) {
      ICode.quad.addLine(e1.A_ADDR, e1.A_IDX, e1.ADDR, "stidx" + dataWidth);
    }
  }

  /**
   * 
   * @param label
   */
  private String getPrePostOp(String label) {
    switch (this.getLabel()) {
    case "PRE-INC":
    case "POST-INC":
      return "+";
    case "PRE-DEC":
    case "POST-DEC":
      return "-";
    default:
      throw new IllegalArgumentException(label + " is an invalid pre/post operator label.");
    }
  }

  @Override
  public String toString() {
    return this.getType() + " " + this.getLabel();
  }
}
