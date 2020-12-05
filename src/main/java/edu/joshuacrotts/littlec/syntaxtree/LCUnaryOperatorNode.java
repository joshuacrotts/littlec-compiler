package edu.joshuacrotts.littlec.syntaxtree;

import org.antlr.v4.runtime.ParserRuleContext;

import edu.joshuacrotts.littlec.icode.ICInhAttr;
import edu.joshuacrotts.littlec.icode.ICode;
import edu.joshuacrotts.littlec.main.LCUtilities;
import edu.joshuacrotts.littlec.main.SymbolTable;

public class LCUnaryOperatorNode extends LCSyntaxTree {

  /** Unary operator to use on r-value. */
  private String op;

  /**
   * Creates a unary operator node. These consist of +, -, !, ~ (bitwise neg), and #.
   * 
   * Child 1 is the unary operator in parenthesis with single quotes.
   * 
   * Child 2 is the type of value produced.
   * 
   * - (+) and (-) can operate on chars and ints with no casting. - ! on a char
   * requires a cast. - # can only be used on an array.
   * 
   * @param ctx         - ParserRuleContext that visits this listener.
   * @param symbolTable - SymbolTable.
   * @param op          - unary operator itself.
   * @param rValType    - type of operator on the right side.
   * @param rvalue      - rvalue itself.
   */
  public LCUnaryOperatorNode(ParserRuleContext ctx, SymbolTable symbolTable, String op, String rValType,
      LCSyntaxTree rvalue) {
    super("UNARYOP(\'" + op + "\')", rValType);
    this.op = op;
    /* Handles the one operator that has to be an integer as specified above. */
    if (LCUtilities.isCastable(rValType, "int") && op.equals("!")) {
      LCTypeCastNode cast = new LCTypeCastNode(ctx, rvalue, "int");
      this.addChild(cast);
      // Reset the type of this cast node to what we cast it to.
      this.setType(cast.getType());
    } else {
      /* One child for the operand of the unary operator. */
      super.addChild(rvalue);
    }
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

    // E.addr = new Temp()
    int dataWidth = LCUtilities.getDataWidth(this.getType());
    e.ADDR = ICode.getTopAR().addTemporaryVariable(dataWidth);

    // E1
    ICInhAttr e1 = new ICInhAttr();
    e1.TYPE = e.TYPE; // Pass down the inherited type.

    // If the operator is a negation, go ahead and apply the
    // flips.
    if (op.equals("!")) {
      e1.TRUE = e.FALSE;
      e1.FALSE = e.TRUE;
    }

    this.getChildren().get(0).genCode(e1);

    // E.code = e1.code
    e.CODE = e1.CODE;

    // If the operator contains a #, we need to know if we have to use the
    // address symbol. If it's a parameter, it's passed by reference so
    // we can just store that reference in a temp (little bit wasteful but it's
    // fine).
    if (op.equals("#")) {
      String addrOp = e1.ADDR.startsWith("p") ? "=" : "&";
      ICode.quad.addLine(e.ADDR, e1.ADDR, addrOp);
      e1.ADDR = e.ADDR;
    }

    // If the operator is just flipping the jump labels,
    // then there's no need to put it (it's superfluous).
    //
    // However, if it's a FUNCTION CALL... we can put it.
    // This will result in a superfluous operator but it's fine.
    if (this.getChildren().get(0) instanceof LCFunctionCallNode || !op.contains("!")) {
      ICode.quad.addLine(e.ADDR, e1.ADDR, this.op);
    }
  }

  @Override
  public String toString() {
    return this.getType() + " " + this.getLabel();
  }
}
