package edu.joshuacrotts.littlec.syntaxtree;

import org.antlr.v4.runtime.ParserRuleContext;

public class LCParameterDeclarationNode extends LCSyntaxTree {

  /**
   * Creates a parameter declaration node.
   * 
   * @param id
   * @param varType
   */
  public LCParameterDeclarationNode(ParserRuleContext ctx, String id, String varType) {
    super("PDECL", "void", id + " (" + varType + ")"); // Name of the identifier being declared followed by its type in
                                                       // parenthesis.
  }

  @Override
  public String toString() {
    return super.getType() + " " + super.getLabel() + " " + super.getInfo();
  }
}