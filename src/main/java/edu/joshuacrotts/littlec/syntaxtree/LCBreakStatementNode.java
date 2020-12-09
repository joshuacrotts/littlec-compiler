package edu.joshuacrotts.littlec.syntaxtree;

import org.antlr.v4.runtime.ParserRuleContext;

import edu.joshuacrotts.littlec.icode.ICInhAttr;
import edu.joshuacrotts.littlec.icode.ICode;
import edu.joshuacrotts.littlec.main.CoreType;

public class LCBreakStatementNode extends LCSyntaxTree {

  /**
   * Creates a break statement node.
   * 
   * @param ctx
   */
  public LCBreakStatementNode(ParserRuleContext ctx) {
    super("BREAK", CoreType.VOID); // No third parameter.
  }

  /**
   * 
   */
  @Override
  public void genCode(ICInhAttr info) {
    if (super.isCalled)
      return;
    super.isCalled = true;

    ICode.quad.addLabel("goto " + ICInhAttr.SUCC);
  }

  @Override
  public String toString() {
    return this.getType() + " " + this.getLabel();
  }
}
