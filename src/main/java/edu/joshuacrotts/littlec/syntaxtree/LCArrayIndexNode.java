package edu.joshuacrotts.littlec.syntaxtree;

import org.antlr.v4.runtime.ParserRuleContext;

import edu.joshuacrotts.littlec.icode.ICInhAttr;
import edu.joshuacrotts.littlec.icode.ICode;
import edu.joshuacrotts.littlec.main.LCUtilities;

public class LCArrayIndexNode extends LCSyntaxTree {

  /**
   * Creates an array index dereference node (a[i]).
   * 
   * Child 1 is the array identifier as a syntax tree.
   * 
   * Child 2 is the expression that calculates the index. This can be calculated
   * or a literal char/int.
   * 
   * @param ctx
   * @param arrayType
   * @param arrayIdentifier
   * @param indexExpr
   */
  public LCArrayIndexNode(ParserRuleContext ctx, String arrayType, LCSyntaxTree arrayIdentifier,
      LCSyntaxTree indexExpr) {
    super("AIDX", arrayType); // Parameter 2 is the type of one of the array elements.
                              // No third parameter.
    /* Child 1 is the array identifier (as a syntax tree). */
    super.addChild(arrayIdentifier);

    if (!(indexExpr.getType().equals("int")) && !(LCUtilities.isCastable(indexExpr.getType(), "int"))) {
      this.printError(ctx, "array index expression is invalid.");
      return;
    }

    /* Child 2 is the expression for the index. */
    super.addChild(indexExpr);
  }

  /**
   * 
   */
  @Override
  public void genCode(ICInhAttr info) {
    if (super.isCalled)
      return;
    super.isCalled = true;

    // Generate the temp var to store the address.
    int width = LCUtilities.getDataWidth(this.getType());
    String tmpAddr = ICode.getTopAR().addTemporaryVariable(width);
    String op = "";

    // Generate the expression for the index.
    ICInhAttr e1 = new ICInhAttr();
    ICInhAttr e2 = new ICInhAttr();

    this.getChildren().get(0).genCode(e1);
    this.getChildren().get(1).genCode(e2);

    // Because arrays are *always* passed by reference, we don't need the address op
    // with a parameter.
    op = e1.ADDR.startsWith("p") ? "=" : "&";
    ICode.quad.addLine(tmpAddr, e1.ADDR, op);
    info.ADDR = tmpAddr;

    if (!info.TYPE.equals("LVAL")) {
      String oldTmp = tmpAddr;
      tmpAddr = ICode.getTopAR().addTemporaryVariable(width);
      info.CODE = e2.ADDR + " ldidx" + width + " ";
      info.ADDR = tmpAddr;
      info.A_ADDR = oldTmp; // Save the location and idx for lookup later.
      info.A_IDX = e2.ADDR;
      ICode.quad.addLine(info.ADDR, oldTmp, e2.ADDR, "ldidx" + width);
    } else {
      info.CODE = e2.ADDR + " stidx" + width + " ";
    }
  }

  @Override
  public String toString() {
    return this.getType() + " " + this.getLabel();
  }
}
