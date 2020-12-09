package edu.joshuacrotts.littlec.syntaxtree;

import org.antlr.v4.runtime.ParserRuleContext;

import edu.joshuacrotts.littlec.icode.ActivationRecord;
import edu.joshuacrotts.littlec.icode.ICInhAttr;
import edu.joshuacrotts.littlec.icode.ICode;
import edu.joshuacrotts.littlec.main.CoreType;

public class LCIfStatementNode extends LCSyntaxTree {

  /**
   * Creates an if statement node.
   * 
   * Child 1 is the conditional of the if statement.
   * 
   * Child 2 is the "then" portion, which is the body of the statement. If there
   * are no braces, this is a single statement (non sequence).
   * 
   * Child 3 is the optional else portion of the if statement, which can be a
   * collection of statements or one statement, but this can also encompass a
   * child with multiple children.
   * 
   * @param ctx
   * @param ifPart
   * @param thenPart
   * @param elsePart
   */
  public LCIfStatementNode(ParserRuleContext ctx, LCSyntaxTree ifPart, LCSyntaxTree thenPart, LCSyntaxTree elsePart) {
    super("IF", CoreType.VOID);// No third parameter.

    /* Two or three children. */
    /* Child 1 is the conditions. */
    super.addChild(ifPart);

    /* Child 2 is the "then" portion of the condition. */
    super.addChild(thenPart);

    /* Child 3 is the "else" part. */
    if (elsePart != null) {
      super.addChild(elsePart);
    }
  }

  /**
   * 
   */
  @Override
  public void genCode(ICInhAttr s) {
    if (super.isCalled)
      return;
    super.isCalled = true;

    // S -> if (b) s1 else s2
    ICInhAttr b = new ICInhAttr();
    ICInhAttr s1 = new ICInhAttr();
    ICInhAttr s2 = new ICInhAttr();

    b.TRUE = ActivationRecord.newLabel();
    b.FALSE = ActivationRecord.newLabel();
    b.NEXT = s.NEXT.isEmpty() ? ActivationRecord.newLabel() : s.NEXT;
    b.TYPE = "IF_COND";

    // Generate the body of the conditional.
    this.getChildren().get(0).genCode(b);

    // The label to goto if true.
    ICode.quad.addLabel(b.TRUE + ":");

    // Body of if.
    this.getChildren().get(1).genCode(s1);

    // Fall through to avoid else.
    ICode.quad.addLabel("goto " + b.NEXT);

    // Goto if we didn't evaluate true in if.
    ICode.quad.addLabel(b.FALSE + ":");

    // Else condition.
    if (this.getChildren().size() > 2) {
      this.getChildren().get(2).genCode(s2);
    }
    ICode.quad.addLabel(b.NEXT + ":");

  }

  @Override
  public String toString() {
    return this.getType() + " " + this.getLabel();
  }
}
