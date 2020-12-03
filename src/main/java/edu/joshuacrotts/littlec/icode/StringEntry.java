package edu.joshuacrotts.littlec.icode;

import edu.joshuacrotts.littlec.main.LCUtilities;

/**
 * StringEntry represents a string in the intermediate code generation stage.
 * Each string has an identifier (user-specified), char values (generated from
 * the algorithm), and a compiler-generated name. The char values come from the
 * ascii values of each character inside the string.
 * 
 * @author Joshua Crotts
 */
public class StringEntry {

  /** Identifier of string. */
  private String id;

  /** Byte declarations of each character (ASCII) in the string. */
  private String charValues;

  /** Compiler-generated identifier of the variable. */
  private String compilerID;

  public StringEntry(String id, String compilerID) {
    // We first need to escape the string to remove
    // \n or \0 if it exists.
    String escpStr = LCUtilities.escapeString(id);
    // Strips the first and last characters (quotes) from the string.
    this.id = escpStr.substring(1, escpStr.length() - 1);
    this.compilerID = compilerID;
    this.charValues = "";
    this.generateCharValues();
  }

  /**
   * Generates the number of characters needed for this variable with the extra
   * null terminator (specified by .dw #).
   * 
   * After this, the ascii value of every character is appended with the .db
   * prefix. The null terminator is appended last.
   * 
   * @param void.
   * 
   * @return void.
   */
  private void generateCharValues() {
    this.charValues += (".dw " + (this.id.length() + 1));
    this.charValues += "\n";

    this.id = LCUtilities.escapeString(this.id);
    StringBuilder sb = new StringBuilder(".db ");
    for (int i = 0; i < this.id.length(); i++) {
      sb.append((int) this.id.charAt(i));
      sb.append(", ");
    }
    sb.append("0");
    this.charValues += sb.toString();
  }

  /**
   * Generates the MIPS code for this particular string. We first need to remove
   * the .db and .dw type directives and replace them with the MIPS equivalents -
   * namely .byte and .word respectively.
   * 
   * @param void.
   * 
   * @return void.
   */
  public String getMIPSCode() {
    String charValuesFiltered = this.charValues.replaceAll(".db", ".byte");
    charValuesFiltered = charValuesFiltered.replaceAll(".dw", ".word");
    return this.compilerID + ":\t" + charValuesFiltered;
  }

  public String getID() {
    return this.id;
  }

  public String getCompilerID() {
    return this.compilerID;
  }

  @Override
  public String toString() {
    return this.charValues;
  }
}