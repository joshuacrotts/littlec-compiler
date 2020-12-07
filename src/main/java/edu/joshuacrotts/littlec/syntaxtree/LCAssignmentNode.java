package edu.joshuacrotts.littlec.syntaxtree;

import org.antlr.v4.runtime.ParserRuleContext;

import edu.joshuacrotts.littlec.icode.ICInhAttr;
import edu.joshuacrotts.littlec.icode.ICode;
import edu.joshuacrotts.littlec.main.LCUtilities;
import edu.joshuacrotts.littlec.main.SymbolTable;

public class LCAssignmentNode extends LCSyntaxTree {

  /**
   * Creates an assignment operator that assigns some expression to an identifier
   * (l-value). Other checks for validity are in LCListener.
   * 
   * @param symbolTable
   * @param id
   * @param idVarType
   * @param expr
   */
  public LCAssignmentNode(ParserRuleContext ctx, SymbolTable symbolTable, String id, String idVarType,
      LCSyntaxTree expr) {
    super("ASN", expr.getType());

    // Adds the l-value to this assignment node.
    this.addChild(new LCVariableIdentifierNode(ctx, symbolTable, id, idVarType));

    // If they types are not equal, we can try to cast the r-value type to match the
    // l-value.
    if (!expr.getType().equals(idVarType)) {
      if (!LCUtilities.isCastable(expr.getType(), idVarType)) {
        this.printError(ctx, "cannot assign " + expr.getType() + " to " + idVarType + ".");
        return;
      } else {
        // If we're trying to assign a char to an int, we need to up-cast the
        // expression.
        LCTypeCastNode cast = new LCTypeCastNode(ctx, expr, idVarType);
        this.addChild(cast);
        this.setType(cast.getType());
      }
    } else {
      this.addChild(expr);
    }
  }

  /**
   * Creates an assignment operator with a new parameter that determines if we're
   * using an array or not. This may or may not be necessary.
   * 
   * @param symbolTable
   * @param id
   * @param idVarType
   * @param arrayIdxNode the l-value of the expression, representing an array
   *                     index node.
   * @param expr         r-value of expression; the expression being evaluated
   *                     itself.
   */
  public LCAssignmentNode(ParserRuleContext ctx, SymbolTable symbolTable, String id, String idVarType,
      LCArrayIndexNode arrayIdxNode, LCSyntaxTree expr) {
    super("ASN", expr.getType());

    /* Get the element type of the array. */
    String elementType = LCUtilities.getArrayType(idVarType);

    // Add the array index node as the l-value.
    this.addChild(arrayIdxNode);

    // Cast it if we can.
    if (!expr.getType().equals(elementType)) {
      if (!LCUtilities.isCastable(expr.getType(), arrayIdxNode.getType())) {
        this.printError(ctx, "cannot assign " + expr.getType() + " to " + idVarType + ".");
      } else {
        LCTypeCastNode cast = new LCTypeCastNode(ctx, expr, arrayIdxNode.getType());
        this.addChild(cast);
        this.setType(cast.getType());
      }
    } else {
      // If we're not casting, just add the r-value expr (it's embedded in the cast if
      // we are doing that).
      this.addChild(expr);
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

    ICInhAttr s = new ICInhAttr();
    ICInhAttr e = new ICInhAttr();

    s.TYPE = "LVAL";

    // S = id + E
    this.getChildren().get(0).genCode(s);
    this.getChildren().get(1).genCode(e);

    // Generate the assignment. If our lvalue is an array
    // then we append the store or load command here.
    if (this.getChildren().get(0) instanceof LCArrayIndexNode) {
      String[] args = s.CODE.split(" ");
      ICode.quad.addLine(s.ADDR, args[0], e.ADDR, args[1]);
    } else {
      ICode.quad.addLine(s.ADDR, e.ADDR, "=");
    }
    info.ADDR = e.ADDR;
  }

  @Override
  public String toString() {
    return this.getType() + " " + this.getLabel();
  }
}
