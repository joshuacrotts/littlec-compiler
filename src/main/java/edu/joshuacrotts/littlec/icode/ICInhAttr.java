package edu.joshuacrotts.littlec.icode;

/**
 * This class acts as an inherited attribute. Everything is public because it's
 * easier to set things that way.
 * 
 * @author Joshua Crotts
 */
public class ICInhAttr {

  /* */
  public static String SUCC = ""; // Next label in line if we break.

  /* */
  public String TYPE = "";

  /* */
  public String REL_TYPE = "";

  /* */
  public String ADDR = "";

  /* */
  public String CODE = "";

  /* */
  public String TRUE = ""; // Label to jump to if expr is true.

  /* */
  public String FALSE = ""; // Label to jump to if expr is false.

  /* */
  public String NEXT = "";

  public ICInhAttr() {
  }

  public ICInhAttr(String type) {
    super();
    this.TYPE = type;
  }
}
