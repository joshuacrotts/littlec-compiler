package edu.joshuacrotts.littlec.main;

import java.util.List;

import edu.joshuacrotts.littlec.syntaxtree.LCSyntaxTree;

/**
 * 
 * @author Joshua Crotts
 */
public class SymbolEntry {

  /* Type of symbol (variable, function). */
  private final String TYPE;

  /*
   * Data type associated with the symbol (e.g. for a variable, it's the data
   * type, and for a function, it's the return type.
   */
  private final String VAR_TYPE;

  /*
   * Storage class for the entry into the table. If this is not set in the
   * listener, then it is the default type. Types are default, static, and extern.
   */
  private final String STORAGE_CLASS;

  /*
   * Other info about the symbol, generally for parameters of a fn. When a
   * function is called, its parameters are checked against this list.
   */
  private final List<LCSyntaxTree> INFO;

  /**
   * Constructs a SymbolEntry with a type, variable type, and a list for other
   * miscellaneous information. This information is generally for storing the
   * arguments for a function.
   * 
   * @param type
   * @param varType
   * @param info
   */
  public SymbolEntry(String type, String varType, String storageClass, List<LCSyntaxTree> info) {
    this.TYPE = type;
    this.VAR_TYPE = varType;
    this.STORAGE_CLASS = storageClass;
    this.INFO = info;
  }

  /**
   * Constructs a SymbolEntry with a type, variable type, and non-default storage
   * class.
   * 
   * @param type
   * @param varType
   * @param storageClass
   */
  public SymbolEntry(String type, String varType, String storageClass) {
    this(type, varType, storageClass, null);
  }

  /**
   * Constructs a SymbolEntry with a type, variable type, and the default storage
   * class.
   * 
   * @param type
   * @param varType
   */
  public SymbolEntry(String type, String varType) {
    this(type, varType, "", null);
  }

  public String getType() {
    return this.TYPE;
  }

  public String getVarType() {
    return this.VAR_TYPE;
  }

  public String getStorageClass() {
    return this.STORAGE_CLASS;
  }

  public List<LCSyntaxTree> getInfoList() {
    return this.INFO;
  }
}