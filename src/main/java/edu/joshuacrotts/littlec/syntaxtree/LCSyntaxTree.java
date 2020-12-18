package edu.joshuacrotts.littlec.syntaxtree;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.antlr.v4.runtime.ParserRuleContext;

import edu.joshuacrotts.littlec.icode.Generatable;
import edu.joshuacrotts.littlec.icode.ICInhAttr;
import edu.joshuacrotts.littlec.main.LCMasks;

/**
 * Syntax tree class - the purpose of the parser is to construct a syntax tree
 * for any valid LittleC program (and detect errors for invalid programs).
 *
 * @author Joshua Crotts
 */
public class LCSyntaxTree implements Generatable {

  /**
   * Flag keeping track of when a genCode() method has been called. Once
   * generated, we shouldn't call it again in the tree traversal.
   */
  protected boolean isCalled = false;
  
  /**
   * Flags for the LCSyntaxTree. We share this one instance across the entire
   * syntax tree so we don't assign flags to part of the tree. If one part fails,
   * the entire tree fails.
   */
  private static int flags = 0;
  
  /** 
   * Label specified by the syntax tree documentation. 
   */
  private String label;

  /** 
   * Type specified by the syntax tree documentation. 
   */
  private String type;

  /** 
   * Info specified by the syntax tree documentation. This may be null. 
   */
  private String info;

  /** 
   * Children of this syntax tree. 
   */
  private List<LCSyntaxTree> children;

  /**
   * This is the parent node for the tree. This is the first node called. Any
   * subsequent children are appended to the list.
   */
  public LCSyntaxTree() {
    this("SEQ", "void");
  }

  /**
   * Creates a new syntax tree node with a previously-determined label and type.
   * No other info is stored with this type of node.
   * 
   * @param label - label specified by the documentation.
   * @param type  - type specified by the documentation.
   */
  public LCSyntaxTree(String label, String type) {
    this(label, type, null);
  }

  /**
   * Creates a new node to store in the syntax tree with extra information. The
   * child linked list is also instantiated here.
   * 
   * @param label - String label specified by the documentation (syntax tree).
   * @param type  - String type specified by the documentation (syntax tree).
   * @param info  - String of extra info specified by the documentation (syntax
   *              tree).
   */
  public LCSyntaxTree(String label, String type, String info) {
    this.label = label;
    this.type = type;
    this.info = info;
    this.children = new LinkedList<>();
  }

  /**
   * Generates the code for the children of this LCSyntaxTree.
   * 
   * @param info
   */
  @Override
  public void genCode(ICInhAttr info) {
    if (this.isCalled)
      return;
    this.isCalled = true;
    for (LCSyntaxTree ch : this.getChildren()) {
      ch.genCode(info);
    }
  }

  /**
   * A method which will print this syntax tree. Since you need to print the top
   * node, it's children, their children, their children, ... this is obviously
   * going to have to be recursive, probably through a recursive helper function.
   */
  public void printSyntaxTree() {
    this.printSyntaxTreeHelper(this, 0);
  }

  /**
   * Prints out the syntax tree recursively with spaces.
   * 
   * @param tree
   * @param spacing
   */
  public void printSyntaxTreeHelper(LCSyntaxTree tree, int spacing) {
    /* Prints out pretty spacing. */
    for (int i = 0; i < spacing; i++)
      System.out.print(" ");
    System.out.print(tree);

    /* Gets an iterator for the children. */
    Iterator<LCSyntaxTree> it = tree.children.iterator();

    if (!it.hasNext()) {
      spacing -= 2;
      return;
    }

    /* Prints out the next ones recursively. */
    System.out.println(" (");
    while (it.hasNext()) {
      LCSyntaxTree next = it.next();
      if (!next.getChildren().isEmpty())
        spacing += 2;

      printSyntaxTreeHelper(next, spacing);
      if (it.hasNext()) {
        System.out.println(", ");
      }
    }
    System.out.print(")");
  }
  
  /**
   * Adds a predefined LCSyntaxTree node to the current LinkedList of children.
   * 
   * @param node
   */
  public void addChild(LCSyntaxTree node) {
    this.children.add(node);
  }

  /**
   * Toggles a flag off.
   * 
   * @param flag
   */
  public void turnOffFlags(int flag) {
    LCSyntaxTree.flags &= ~flag;
  }

  /**
   * Resets all flags to 0 except the error mask; if it's enabled, we leave it be.
   */
  public void clearFlags() {
    LCSyntaxTree.flags &= 0;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getInfo() {
    return info;
  }

  public void setInfo(String info) {
    this.info = info;
  }

  public void setFlags(int flag) {
    LCSyntaxTree.flags |= flag;
  }

  public int getFlags() {
    return LCSyntaxTree.flags;
  }

  public boolean isInteger() {
    return this.getType().equals("int");
  }

  public boolean isChar() {
    return this.getType().equals("char");
  }

  public boolean isArray() {
    return this.getType().endsWith("[]") || (this.getType().indexOf("[") < this.getType().indexOf("]"));
  }

  public List<LCSyntaxTree> getChildren() {
    return this.children;
  }

  @Override
  public String toString() {
    if (this.info != null)
      return this.type + " " + this.label + (this.getChildren().isEmpty() ? " ()" : "") + " " + this.info;
    else
      return this.type + " " + this.label + (this.getChildren().isEmpty() ? " ()" : "");
  }
}
