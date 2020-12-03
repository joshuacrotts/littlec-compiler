package edu.joshuacrotts.littlec.syntaxtree;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;

import edu.joshuacrotts.littlec.main.SymbolEntry;
import edu.joshuacrotts.littlec.main.SymbolTable;

public class LCFunctionPrototypeNode extends LCSyntaxTree {

  /**
   * Creates a forward declaration of a function, otherwise known as a function
   * prototype. These nodes are not in the syntax tree; they are only for
   * reference by other functions if the declaration is not currently in scope.
   * 
   * @param ctx
   * @param symbolTable
   * @param id
   * @param varType
   * @param storageClass
   * @param args
   */
  public LCFunctionPrototypeNode(ParserRuleContext ctx, SymbolTable symbolTable, String id, String retType,
      String storageClass, LinkedHashMap<String, String> args) {
    super("FNPROTOTYPE", retType, id);

    /*
     * If we don't have the symbol in the table, then we're good to add it. The
     * scope of a functions is always global.
     */
    if (!symbolTable.hasSymbol(id)) {
      List<LCSyntaxTree> argsList = new LinkedList<>();

      for (String key : args.keySet()) {
        argsList.add(new LCVariableIdentifierNode(ctx, symbolTable, key, args.get(key)));
      }

      symbolTable.addSymbol(id, new SymbolEntry("FNPROTOTYPE", retType, storageClass, argsList));
    } else {
      this.printError(ctx, id + " has already been declared in this scope.");
    }
  }

  @Override
  public String toString() {
    return this.getType() + " ID " + this.getInfo();
  }
}