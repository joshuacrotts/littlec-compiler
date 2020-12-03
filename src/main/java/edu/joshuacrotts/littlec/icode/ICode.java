package edu.joshuacrotts.littlec.icode;

import java.util.Stack;

import edu.joshuacrotts.littlec.syntaxtree.LCSyntaxTree;

/**
 * Class for intermediate code. The two most important external interfaces to
 * this code are the constructor and the toString method -- see below for more
 * information. You can add other methods, fields, or code as you see fit.
 *
 * @author Joshua Crotts
 */
public class ICode {

  /* Quadruple of program 3 address code statements. */
  public static Quadruple quad = new Quadruple();

  /* Stack of activation records. */
  private static Stack<ActivationRecord> arStack = new Stack<>();

  /**
   * The constructor takes a syntax tree, and creates some internal representation
   * of intermediate code. While the representation is up to you, I strongly
   * encourage you to keep the code structured. In the final phase you'll have to
   * do some basic analysis on the intermediate code in order to produce the
   * target code, and that's hard if you're just storing strings (for example).
   *
   * @param tree the syntax tree for the input program
   */
  public ICode(LCSyntaxTree tree) {
    arStack.push(new ActivationRecord());
    this.traverse(tree);
  }

  /**
   * 
   * @param ar
   */
  public static void addAR(ActivationRecord ar) {
    arStack.add(ar);
  }

  /**
   * 
   * @return
   */
  public static ActivationRecord getTopAR() {
    return arStack.peek();
  }

  /**
   * 
   */
  public static void removeTopAR() {
    arStack.pop();
  }

  /**
   * 
   * @return
   */
  public static int getARStackSize() {
    return arStack.size();
  }

  /**
   * 
   */
  public static void cleanup() {
    arStack.clear();
    quad.cleanup();
  }

  /**
   * 
   * @param tree
   */
  private void traverse(LCSyntaxTree tree) {
    this.preOrderGenCodeHelper(tree);
  }

  /**
   * 
   * @param tree
   */
  private void preOrderGenCodeHelper(LCSyntaxTree tree) {
    tree.genCode(new ICInhAttr());

    for (LCSyntaxTree child : tree.getChildren()) {
      this.preOrderGenCodeHelper(child);
    }
  }

  /**
   * This performs the important function of turning the internal form of your
   * intermediate code into a text (readable) format.
   *
   * @return the code, as a long multi-line string
   */
  @Override
  public String toString() {
    return quad.toString();
  }
}