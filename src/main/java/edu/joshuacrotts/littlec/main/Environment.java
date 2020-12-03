package edu.joshuacrotts.littlec.main;

import java.util.TreeMap;

/**
 * An Environment is a TreeMap of identifiers with SymbolEntry values mapped
 * together. Environments are used to keep track of variables as new scopes are
 * generated in the code (i.e. when new blocks are made, as well as the start
 * and end of functions).
 * 
 * @author Joshua Crotts
 */
public class Environment {

  /** HashMap of identifiers and SymbolEntry values. */
  private TreeMap<String, SymbolEntry> symbols;

  /**
   * Creates the HashMap in a new Environment. An Environment is a block of code
   * with the exception of statements like if, else, for, and while. Blocks,
   * therefore, are function declarations, and the global scope. These are added
   * and popped from the Environment stack in the symbol table.
   */
  public Environment() {
    this.symbols = new TreeMap<>();
  }

  /**
   * Adds an identifier and a SymbolEntry to the symbol table.
   * 
   * @param id
   * @param type
   */
  public void addSymbol(String id, SymbolEntry type) {
    this.symbols.put(id, type);
  }

  /**
   * Returns whether or not the map contains an identifier.
   * 
   * @param id
   * @return true if the key exists, false otherwise.
   */
  public boolean hasSymbol(String id) {
    return this.symbols.containsKey(id);
  }

  /**
   * Prints all identifiers in the map (environment).
   */
  public void printEnvironmentIDs() {
    for (String treeKey : this.symbols.keySet()) {
      System.out.println(treeKey);
    }
  }

  public TreeMap<String, SymbolEntry> getTreeMap() {
    return this.symbols;
  }
}
