package edu.joshuacrotts.littlec.syntaxtree;

import org.antlr.v4.runtime.ParserRuleContext;

import edu.joshuacrotts.littlec.icode.ActivationRecord;
import edu.joshuacrotts.littlec.icode.ICInhAttr;
import edu.joshuacrotts.littlec.icode.ICode;
import edu.joshuacrotts.littlec.main.LCUtilities;
import edu.joshuacrotts.littlec.main.SymbolTable;

public class LCBinaryOperatorNode extends LCSyntaxTree {

  /** 
   * Binary operator that we're using. 
   */
  private String op;

  /**
   * Creates a binary operator node with the type of operator and its children.
   * 
   * Child 1 is the left operand.
   * 
   * Child 2 is the right operand.
   * 
   * There are two types of operators: those that perform an algebraic
   * computation, and boolean operators (i.e. comparison operators). The latter
   * always produce an integer without casting.
   * 
   * If both operands are chars, the result is a char. If only one is an integer,
   * the other is up-casted to a char.
   * 
   * @param ctx
   * @param symbolTable
   * @param op
   * @param lOperand
   * @param rOperand
   * @param isComparsionOp
   */
  public LCBinaryOperatorNode(ParserRuleContext ctx, SymbolTable symbolTable, String op, LCSyntaxTree lOperand,
      LCSyntaxTree rOperand) {
    super("BINOP(\'" + op + "\')", null);
    this.op = op;

    boolean isComparisonOp = LCUtilities.isComparisonOp(this.op);

    // If one operand is a char, we promote it to an int.
    // If both are chars, the binary operator returns an int.
    // If only one is a char, then it's casted to an integer.
    if (lOperand.getType().equals(rOperand.getType())) {
      this.setType(lOperand.getType());
    } else {
      if (lOperand.isChar()) {
        lOperand = new LCTypeCastNode(ctx, lOperand, "int");
      } else if (rOperand.isChar()) {
        rOperand = new LCTypeCastNode(ctx, rOperand, "int");
      }
      this.setType("int");
    }

    // Comparison operators ALWAYS, no matter WHAT, result in an integer.
    if (isComparisonOp) {
      this.setType("int");
    }

    this.addChild(lOperand);
    this.addChild(rOperand);
  }

  /**
   * 
   */
  @Override
  public void genCode(ICInhAttr e) {
    if (super.isCalled) {
      return;
    }
    super.isCalled = true;

    /* If it's a comparison operator, set up the short-circuit evaluation. */
    if (LCUtilities.isComparisonOp(this.op)) {
      ICInhAttr b1 = new ICInhAttr();
      ICInhAttr b2 = new ICInhAttr();

      e.TRUE = e.TRUE.isEmpty() ? ActivationRecord.newLabel() : e.TRUE;
      e.FALSE = e.FALSE.isEmpty() ? ActivationRecord.newLabel() : e.FALSE;
      b1.REL_TYPE = b2.REL_TYPE = this.op;

      // If we're not inside a conditional or loop, then we can safely
      // generate a temporary variable.
      if (e.TYPE.isEmpty()) {
        int width = LCUtilities.getDataWidth(this.getType());
        e.ADDR = ICode.getTopAR().addTemporaryVariable(width);
        b1.ADDR = b2.ADDR = e.ADDR;
      }

      // In order to short-circuit, we need to reassign/pass-down
      // attributes (i.e. inherited attributes...).
      if (op.equals("&&")) {
        b1.TRUE = ActivationRecord.newLabel();
        b1.FALSE = e.FALSE;
        b2.TRUE = e.TRUE;
        b2.FALSE = e.FALSE;
        this.getChildren().get(0).genCode(b1);
        ICode.quad.addLabel(b1.TRUE + ":");
        this.getChildren().get(1).genCode(b2);
      } else {
        b1.TRUE = e.TRUE;
        b1.FALSE = ActivationRecord.newLabel();
        b2.TRUE = e.TRUE;
        b2.FALSE = e.FALSE;
        this.getChildren().get(0).genCode(b1);
        ICode.quad.addLabel(b1.FALSE + ":");
        this.getChildren().get(1).genCode(b2);
      }

      // If we're not in an IF then we generate the temp
      // vars.
      if (e.TYPE.isEmpty()) {
        String n = ActivationRecord.newLabel();
        ICode.quad.addLabel(b2.TRUE + ":");
        ICode.quad.addLine(e.ADDR, "1", "=");
        ICode.quad.addLabel("goto " + n);
        ICode.quad.addLabel(b2.FALSE + ":");
        ICode.quad.addLine(e.ADDR, "0", "=");
        ICode.quad.addLabel(n + ":");
      }
    }
    /* Relops need to set up the "fall" scenario. */
    else if (LCUtilities.isRelationalOp(op)) {
      ICInhAttr e1 = new ICInhAttr();
      ICInhAttr e2 = new ICInhAttr();

      this.getChildren().get(0).genCode(e1);
      this.getChildren().get(1).genCode(e2);

      // If we're not in an if and we have no logic operators then
      // we need to print the temp labels here.
      if (e.TYPE.isEmpty() && e.REL_TYPE.isEmpty()) {
        int width = LCUtilities.getDataWidth(this.getType());
        e.ADDR = ICode.getTopAR().addTemporaryVariable(width);
        String t = e.TRUE.isEmpty() ? ActivationRecord.newLabel() : e.TRUE;
        String f = e.FALSE.isEmpty() ? ActivationRecord.newLabel() : e.FALSE;
        String n = ActivationRecord.newLabel();
        ICode.quad.addLine("goto " + t, e1.ADDR, e2.ADDR, "if" + op); // TRUE
        ICode.quad.addLabel("goto " + f); // FALSE
        ICode.quad.addLabel(t + ":");
        ICode.quad.addLine(e.ADDR, "1", "=");
        ICode.quad.addLabel("goto " + n); // NEXT
        ICode.quad.addLabel(f + ":");
        ICode.quad.addLine(e.ADDR, "0", "=");
        ICode.quad.addLabel(n + ":");
      } else {
        ICode.quad.addLine("goto " + e.TRUE, e1.ADDR, e2.ADDR, "if" + op);
        ICode.quad.addLabel("goto " + e.FALSE);
      }
    }
    /* Anything else is just normal. */
    else {
      ICInhAttr e1 = new ICInhAttr();
      ICInhAttr e2 = new ICInhAttr();
      e2.TYPE = e1.TYPE = "RVAL";

      // E1 + E2
      this.getChildren().get(0).genCode(e1);
      this.getChildren().get(1).genCode(e2);

      // E.addr = new Temp();
      int width = LCUtilities.getDataWidth(this.getType());
      e.ADDR = ICode.getTopAR().addTemporaryVariable(width);

      // gen(E.addr = E1.addr op E2.addr);
      ICode.quad.addLine(e.ADDR, e1.ADDR, e2.ADDR, this.op);
    }
  }

  @Override
  public String toString() {
    return this.getType() + " " + this.getLabel();
  }
}
