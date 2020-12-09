package edu.joshuacrotts.littlec.syntaxtree;

import org.antlr.v4.runtime.ParserRuleContext;

import edu.joshuacrotts.littlec.main.CoreType;

public class LCParameterDeclarationNode extends LCSyntaxTree {

  /**
   * Creates a parameter declaration node.
   * 
   * @param id
   * @param varType
   */
  public LCParameterDeclarationNode(ParserRuleContext ctx, String id, CoreType varType) {
    super("PDECL", CoreType.VOID, id + " (" + varType + ")"); // Name of the identifier being declared followed by its type in
                                                       // parenthesis.
  }

  @Override
  public String toString() {
    return super.getType() + " " + super.getLabel() + " " + super.getInfo();
  }
}
