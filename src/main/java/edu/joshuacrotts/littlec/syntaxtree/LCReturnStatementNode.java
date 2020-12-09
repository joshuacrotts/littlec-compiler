package edu.joshuacrotts.littlec.syntaxtree;

import org.antlr.v4.runtime.ParserRuleContext;

import edu.joshuacrotts.littlec.icode.ICInhAttr;
import edu.joshuacrotts.littlec.icode.ICode;
import edu.joshuacrotts.littlec.main.CoreType;
import edu.joshuacrotts.littlec.main.LCUtilities;

public class LCReturnStatementNode extends LCSyntaxTree {

  /**
   * Creates a return statement node.
   * 
   * Child 1: Optional return value. Since this node is only created when a return
   * statement is required, this is effectively mandatory.
   * 
   * @param ctx
   * @param returnVal
   */
  public LCReturnStatementNode(ParserRuleContext ctx, LCSyntaxTree returnVal) {
    super("RETURN", CoreType.VOID);

    // Third child is the optional return value.
    if (returnVal != null) {
      super.addChild(returnVal);
    } else {
      this.setLabel("RETURN ()");
    }
  }

  /**
   * 
   */
  @Override
  public void genCode(ICInhAttr info) {
    if (super.isCalled)
      return;
    super.isCalled = true;

    // Generate the return string.
    String returnStr = "return";

    // If there is a return expression, then we have to append a data width to it.
    // Otherwise,
    if (!this.getChildren().isEmpty()) {
      this.getChildren().get(0).genCode(info);
      int retWidth = this.getChildren().get(0).getType().getWidth();
      returnStr += retWidth;
    }

    // addLine(resAddr, op1, op2, op), "return" is the op, return val is op1.
    ICode.quad.addLine("", (!this.getChildren().isEmpty() ? info.ADDR : ""), "", returnStr);
  }

  @Override
  public String toString() {
    return this.getType() + " " + this.getLabel();
  }
}
