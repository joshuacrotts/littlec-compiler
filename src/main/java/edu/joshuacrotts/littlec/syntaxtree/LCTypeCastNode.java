package edu.joshuacrotts.littlec.syntaxtree;

import org.antlr.v4.runtime.ParserRuleContext;

import edu.joshuacrotts.littlec.icode.ICInhAttr;
import edu.joshuacrotts.littlec.icode.ICode;
import edu.joshuacrotts.littlec.main.CoreType;
import edu.joshuacrotts.littlec.main.LCUtilities;

public class LCTypeCastNode extends LCSyntaxTree {

  /**
   * Casts a node between one type to another. Type verification should be
   * performed before this node is constructed. LCUtilities provides a method to
   * determine if it's possible to cast between one type and another.
   * 
   * @param ctx
   * @param rvalue
   * @param targetType
   */
  public LCTypeCastNode(ParserRuleContext ctx, LCSyntaxTree rvalue, CoreType targetType) {
    super("CAST", targetType); // No third parameter.

    /* One child for the value that is casted. */
    super.addChild(rvalue);
  }

  /**
   * 
   */
  @Override
  public void genCode(ICInhAttr info) {
    if (super.isCalled) {
      return;
    }
    super.isCalled = true;

    CoreType rvalType = this.getChildren().get(0).getType();
    String castType = "";

    if (rvalType.getPriority() < this.getType().getPriority()) {
      castType = "widen";
    } else if (rvalType.getPriority() > this.getType().getPriority()) {
      castType = "narrow";
    } else {
      castType = "&";
    }

    // Generate the cast variable temp.
    int castWidth = this.getType().getWidth();
    String tmpCastVar = ICode.getTopAR().addTemporaryVariable(castWidth);

    // Generate the r-value that we're going to cast.
    this.getChildren().get(0).genCode(info);

    // Actually cast the variable.
    ICode.quad.addCast(tmpCastVar, info.ADDR, castType);
    info.ADDR = tmpCastVar;
  }

  @Override
  public String toString() {
    return this.getType() + " " + this.getLabel();
  }
}
