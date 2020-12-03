package edu.joshuacrotts.littlec.icode;

public interface Generatable {

  /**
   * Generates the intermediate code for one node in the syntax tree.
   * 
   * @return lvalue address of 3AC.
   */
  public void genCode(ICInhAttr code);
}
