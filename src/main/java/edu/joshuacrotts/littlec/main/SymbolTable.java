package edu.joshuacrotts.littlec.main;

import java.util.List;
import java.util.Stack;
import java.util.TreeMap;

import edu.joshuacrotts.littlec.syntaxtree.LCSyntaxTree;

/**
 * Symbol Table class for the LittleC compiler project. Implementation details
 * are up to you, but make sure you fill in the printGlobalVars() and
 * printGlobalFns() methods below.
 */
public class SymbolTable {

  /*
   * Stack of environment objects to represent the call-stack/scope for variables
   * and functions. Each time a variable or function is referenced, it is checked
   * against this table. Variables are checked in the top-most stack for existence
   * and it traverses to the bottom until they are either found or not. Functions
   * are simply checked for existence but if they are declared twice, then we
   * throw an error (this does not happen yet).
   */
  private Stack<Environment> environmentTable = new Stack<Environment>();

  /**
   * Given an identifier and a SymbolEntry object, we push it to the top-most
   * environment in the stack.
   * 
   * @param id   - identification of the symbol.
   * @param type - SymbolEntry object with the TYPE of object (fn, variable), and
   *             the datatype associated with is symbol.
   */
  public void addSymbol(String id, SymbolEntry type) {
    this.environmentTable.peek().addSymbol(id, type);
  }

  /**
   * Given an identifier, we return if the symbol is declared inside the current
   * environment. This is useful for determining if we have a variable previously
   * declared in the same scope, while allowing for variable shadowing.
   * 
   * @param id - identifier of symbol.
   * @return true if the identifier was found in the current environment (defined
   *         as the top-most environment on the stack), false otherwise.
   */
  public boolean hasSymbolInCurrentEnvironment(String id) {
    return this.environmentTable.peek().hasSymbol(id);
  }

  /**
   * Given an identifier, we return if the symbol is declared anywhere in the
   * symbol table.
   * 
   * @param id - identifier of symbol.
   * 
   * @return true if the symbol is in any environment stack, false otherwise.
   */
  public boolean hasSymbol(String id) {
    boolean found = false;

    // We have to use a for loop to traverse backwards since iterators are broken
    // with stacks...
    for (int i = this.environmentTable.size() - 1; i >= 0; i--) {
      Environment curr = this.environmentTable.get(i);
      found = hasSymbolInEnvironment(id, curr);

      if (found) {
        return true;
      }
    }

    return false;
  }

  /**
   * Given a symbol ID, returns the SymbolEntry object. This is useful for
   * comparing datatypes of a variable, function, etc.
   * 
   * @param id - identifier of symbol.
   * 
   * @return SymbolEntry value for identifier key.
   */
  public SymbolEntry getSymbolEntry(String id) {
    for (int i = this.environmentTable.size() - 1; i >= 0; i--) {
      Environment curr = this.environmentTable.get(i);

      for (String key : curr.getTreeMap().keySet()) {
        if (id.equals(key)) {
          return curr.getTreeMap().get(key);
        }
      }
    }

    return null;
  }

  /**
   * Adds a new environment to the stack. This is really only useful for functions
   * since variable declaration isn't allowed in an if statement or while loop.
   */
  public void addEnvironment() {
    this.environmentTable.push(new Environment());
  }

  /**
   * Adds a new, previously-defined environment to the stack. This is useful for
   * if the function being defined has parameters. Otherwise, the previous method
   * works just as well.
   * 
   * @param env
   */
  public void addEnvironment(Environment env) {
    this.environmentTable.push(env);
  }

  /**
   * Removes the top-most environment from the stack. This is useful for going out
   * of scope of a function, for instance (really, this is the ONLY place it is
   * used).
   */
  public void popEnvironment() {
    this.environmentTable.pop();
  }

  /**
   * This method should print out all of the global variables. It can be called
   * after parsing in order to see what global variables were seen. Names should
   * be output in alphabetical order.
   */
  public void printGlobalVars() {
    int stackSize = this.environmentTable.size();
    // Retrieve the bottom of the stack (which is the global block).
    Environment globalEnvironment = this.environmentTable.get(stackSize - 1);

    // Return the set of keys.
    TreeMap<String, SymbolEntry> map = globalEnvironment.getTreeMap();

    // Now iterate through the map and find all vars.
    for (String key : map.keySet()) {
      if (map.get(key).getType().equals("VAR")) {
        CoreType type = this.getSymbolEntry(key).getVarType();
        System.out.println(key + ": " + type);
      }
    }
  }

  /**
   * This method should print out all of the global functions (in LittleC, all
   * functions are global, so that means all functions). It can be called after
   * parsing in order to see what functions were defined. This will probably
   * include the standard functions (prints, printd, read, and readline) as well.
   * Names should be printed in alphabetical order.
   */
  public void printGlobalFns() {
    int stackSize = this.environmentTable.size();
    // Retrieve the bottom of the stack which is the global block.
    Environment globalEnvironment = this.environmentTable.get(stackSize - 1);

    // Return the set of keys.
    TreeMap<String, SymbolEntry> map = globalEnvironment.getTreeMap();

    // Now iterate through the map and find all vars.
    for (String key : map.keySet()) {
      if (map.get(key).getType().equals("FNDEF")) {

        // Grab the parameters and return type.
        List<LCSyntaxTree> params = this.getSymbolEntry(key).getInfoList();
        CoreType returnType = this.getSymbolEntry(key).getVarType();
        StringBuilder paramStr = new StringBuilder();

        // Procedurally build the parameter types in parenthesis.
        for (LCSyntaxTree pt : params) {
          paramStr.append(pt.getType());
          paramStr.append(",");
        }

        // If the parameter string is not empty, then cut the end off so we don't have a
        // trailing comma (,).
        if (paramStr.length() != 0) {
          paramStr.setLength(paramStr.length() - 1);
        }

        System.out.println(key + ": FUNCTION " + returnType + " (" + paramStr + ")");
      }
    }
  }

  /**
   * Given an identifier, we return if the symbol is declared inside an arbitrary
   * environment.
   * 
   * @param id
   * @param environment
   * @return
   */
  private boolean hasSymbolInEnvironment(String id, Environment environment) {
    int idx = this.environmentTable.indexOf(environment);
    if (idx < 0 || idx >= this.environmentTable.size()) {
      throw new IndexOutOfBoundsException("idx " + idx + " is out of bounds.");
    }

    return this.environmentTable.get(idx).hasSymbol(id);
  }
}
