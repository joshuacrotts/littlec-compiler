package edu.joshuacrotts.littlec.syntaxtree;

import org.antlr.v4.runtime.ParserRuleContext;

import edu.joshuacrotts.littlec.icode.ICInhAttr;
import edu.joshuacrotts.littlec.icode.ICode;

public class LCConstantLiteralNode extends LCSyntaxTree {

  /**
   * Creates a node for a literal value.
   * 
   * @param ctx
   * @param value
   * @param type
   */
  public LCConstantLiteralNode(ParserRuleContext ctx, String value, String type) {
    super("LIT = " + value, type, value);
  }

  /**
   * 
   */
  @Override
  public void genCode(ICInhAttr e) {
    if (super.isCalled)
      return;
    super.isCalled = true;

    // If our literal is a string, then we need to add it to the string table.
    if (this.getType().equals("char[]")) {
      e.ADDR = ICode.getTopAR().addString(this.getInfo());
    } else {
      // E.addr = lit.
      e.ADDR = this.getInfo();
    }
  }

  @Override
  public String toString() {
    return this.getType() + " " + this.getLabel();
  }
}
