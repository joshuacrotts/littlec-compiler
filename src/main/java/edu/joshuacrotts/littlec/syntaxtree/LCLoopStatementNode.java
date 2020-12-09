package edu.joshuacrotts.littlec.syntaxtree;

import org.antlr.v4.runtime.ParserRuleContext;

import edu.joshuacrotts.littlec.icode.ActivationRecord;
import edu.joshuacrotts.littlec.icode.ICInhAttr;
import edu.joshuacrotts.littlec.icode.ICode;
import edu.joshuacrotts.littlec.main.CoreType;

public class LCLoopStatementNode extends LCSyntaxTree {

  /**
   * Creates a for/while loop node with a conditional and the body. This is also
   * used in the for loop node.
   * 
   * Child 1 is the loop conditional.
   * 
   * Child 2 is a sequence of statements that make up the body of the loop. This
   * can either be one statement or a sequence of many.
   * 
   * @param ctx
   * @param condPart
   * @param loopBody
   */
  public LCLoopStatementNode(ParserRuleContext ctx, LCSyntaxTree condPart, LCSyntaxTree loopBody) {
    super("WHILE", CoreType.VOID);

    /* Child 1 is the loop condition. */
    super.addChild(condPart);

    /* Child 2 is the body of the loop. A sequence of statements to be specific. */
    super.addChild(loopBody);
  }

  /**
   * 
   */
  @Override
  public void genCode(ICInhAttr s) {
    if (super.isCalled)
      return;
    super.isCalled = true;

    // S -> label(TRUE) || if (b) || ...
    // Just the normal loop.
    ICInhAttr b = new ICInhAttr();
    String begin = ActivationRecord.newLabel();
    b.TRUE = s.TRUE.isEmpty() ? ActivationRecord.newLabel() : s.TRUE;
    b.FALSE = s.FALSE.isEmpty() ? ActivationRecord.newLabel() : s.FALSE;
    b.NEXT = s.NEXT.isEmpty() ? ActivationRecord.newLabel() : s.NEXT;
    b.TYPE = "IF_COND";

    // Keep track of which loop we're in so we know where to break.
    ICInhAttr.SUCC = b.FALSE;

    // Print out the start label.
    ICode.quad.addLabel(begin + ":");

    // ...then the conditional.
    this.getChildren().get(0).genCode(b);
    // The label to go to if it's true.
    ICode.quad.addLabel(b.TRUE + ":");
    this.getChildren().get(1).genCode(s);
    ICode.quad.addLabel("goto " + begin);
    ICode.quad.addLabel(b.FALSE + ":");

  }

  @Override
  public String toString() {
    return this.getType() + " " + this.getLabel();
  }
}
