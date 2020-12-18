package edu.joshuacrotts.littlec.syntaxtree;

import java.util.LinkedList;
import java.util.List;

import edu.joshuacrotts.littlec.main.SymbolTable;

public class LCFunctionArgsListNode extends LCSyntaxTree {

  /** 
   * List of LCSyntaxTree arguments. 
   */
  private List<LCSyntaxTree> args;

  /**
   * This node is not used inside the tree class; it's usage comes through passing
   * parameters around from one context to another, hence why it's a LCSyntaxTree.
   * 
   * @param symbolTable
   * @param args
   */
  public LCFunctionArgsListNode(SymbolTable symbolTable, LinkedList<LCSyntaxTree> args) {
    super("FNARGS", "!!!UNUSED!!!");
    this.args = args;
  }

  public List<LCSyntaxTree> getParams() {
    return this.args;
  }
}
