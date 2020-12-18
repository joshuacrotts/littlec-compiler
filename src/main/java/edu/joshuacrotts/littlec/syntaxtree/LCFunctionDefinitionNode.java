package edu.joshuacrotts.littlec.syntaxtree;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;

import edu.joshuacrotts.littlec.icode.ActivationRecord;
import edu.joshuacrotts.littlec.icode.ICInhAttr;
import edu.joshuacrotts.littlec.icode.ICode;
import edu.joshuacrotts.littlec.main.Environment;
import edu.joshuacrotts.littlec.main.LCErrorListener;
import edu.joshuacrotts.littlec.main.LCUtilities;
import edu.joshuacrotts.littlec.main.SymbolEntry;
import edu.joshuacrotts.littlec.main.SymbolTable;

public class LCFunctionDefinitionNode extends LCSyntaxTree {

  /** 
   * Identifier of function. 
   */
  private String id;

  /**
   * List of LCSyntaxTree nodes describing the arguments of this function
   * declaration.
   */
  private List<LCSyntaxTree> argsList;

  /**
   * Creates a new function definition/declaration node. A function declaration
   * must have a body, unlike the prototype.
   * 
   * Child 1 is the body of the function (i.e. a sequence of statements).
   * 
   * @param symbolTable
   * @param id
   * @param varType
   * @param storageClass
   * @param params       - HashMap of identifier/datatype pairs.
   * @param newScope     - Scope of this function call, the current LCSyntaxTree
   */
  public LCFunctionDefinitionNode(ParserRuleContext ctx, SymbolTable symbolTable, String id, String retType,
      String storageClass, LinkedHashMap<String, String> args, LCSyntaxTree newScope) {
    super("FNDEF", retType, id);
    super.addChild(newScope);

    this.id = id;
    this.argsList = new LinkedList<>();

    // Add syntax tree nodes to our list of parameters.
    for (String key : args.keySet()) {
      argsList.add(new LCVariableIdentifierNode(ctx, symbolTable, key, args.get(key)));
    }

    // If we don't have the symbol in the table, then we're good to add it. The
    // scope of a functions is always global.
    if (!symbolTable.hasSymbol(id)) {
      symbolTable.addSymbol(id, new SymbolEntry("FNDEF", retType, storageClass, argsList));
    } else {
      // If the definition already exists, then it has to be a function prototype or
      // it's invalid.
      if (!this.checkParameterMatching(ctx, symbolTable, argsList, id, retType, storageClass)) {
        return;
      }
    }

    this.addArgsToStack(ctx, symbolTable, newScope, args, id, retType);
  }

  /**
   * 
   */
  @Override
  public void genCode(ICInhAttr info) {
    if (super.isCalled)
      return;
    super.isCalled = true;

    // Add the new AR stack for children to add to.
    ICode.addAR(new ActivationRecord());

    // Add the parameters to the AR stack.
    for (LCSyntaxTree params : this.argsList) {
      int width = LCUtilities.getDataWidth(params.getType());
      String id = params.getInfo();
      ICode.getTopAR().addParameterVariable(id, width);
    }

    // Get the line for where we need to insert the function definition
    // with the stack space later.
    int fnDefLineNo = ICode.quad.getNextAvailableLine();

    // Generate the code for the body of the fn.
    this.getChildren().get(0).genCode(info);
    String fnEndLabel = ".fnEnd";
    ICode.quad.addLabel(fnEndLabel);

    // Get the amount of local stack space needed by this function.
    int fnDataSize = ICode.getTopAR().getLocalSpace();

    // Generate the labels with the starting name
    // and the size in bytes of local vars.
    //
    // We need to insert this at the line in the quad before the body
    // of the function.
    String fnLabel = "gf_" + this.id;
    String fnStartLabel = ".fnStart";
    ICode.quad.addLine(fnDefLineNo, fnLabel, fnStartLabel, Integer.toString(fnDataSize),
        Integer.toString(this.argsList.size()));

    // Remove the top AR stack.
    ICode.removeTopAR();
  }

  /**
   * Verifies that a list of parameters match against an existing function
   * prototype.
   * 
   * @param ctx         - ParserRuleContext from above method.
   * @param symbolTable - symbol table to verify the existence of the id.
   * @param argsList    - arguments passed in via the declaration.
   * @param id          - identifier of function.
   * @param retType     - return type of function
   * @return true if parameters were matched without error, false otherwise.
   */
  private boolean checkParameterMatching(ParserRuleContext ctx, SymbolTable symbolTable, List<LCSyntaxTree> argsList,
      String id, String retType, String storageClass) {
    if (symbolTable.getSymbolEntry(id).getType().equals("FNPROTOTYPE")) {
      // We first need to check if the return types match.
      String prototypeReturnType = symbolTable.getSymbolEntry(id).getVarType();
      if (!prototypeReturnType.equals(retType)) {
        LCErrorListener.syntaxError(ctx, "prototype function " + id + " expects return type " + prototypeReturnType
            + ", but the declaration expects " + retType + ".");
        return false;
      }

      // Next, we need to check that the storage classes match.
      String prototypeStorageClass = symbolTable.getSymbolEntry(id).getStorageClass();
      if (!prototypeStorageClass.equals(storageClass)) {
        LCErrorListener.syntaxError(ctx, "prototype function " + id + " expects a storage class of " + prototypeStorageClass
            + ", but the declaration expects " + storageClass + ".");
        return false;
      }

      // If there already exists a prototype, go through and make sure the existing
      // data matches this one.
      LinkedList<LCSyntaxTree> prototypeArgs = (LinkedList<LCSyntaxTree>) symbolTable.getSymbolEntry(id).getInfoList();

      // If the two parameter declarations aren't the same size, then there's no point
      // of continuing.
      if (prototypeArgs.size() != argsList.size()) {
        LCErrorListener.syntaxError(ctx, "prototype function " + id + " expects " + prototypeArgs.size()
            + " arguments, but the declaration expects " + argsList.size() + " arguments.");
        return false;
      }

      // Go through one by one and compare the types.
      for (int i = 0; i < argsList.size(); i++) {
        String fnArg = argsList.get(i).getType();
        String param = prototypeArgs.get(i).getType();

        // Here we run into a small problem with arrays but it's easily solvable.
        if (!fnArg.equals(param)) {
          LCErrorListener.syntaxError(ctx, "declaration for function " + id + " parameter " + (i + 1) + " expects " + fnArg
              + " but function expects " + param + ".");
          return false;
        }
      }

      symbolTable.addSymbol(id, new SymbolEntry("FNDEF", retType, storageClass, argsList));
    } else {
      LCErrorListener.syntaxError(ctx, id + " has already been declared in this scope.");
      return false;
    }

    return true;
  }

  /**
   * Adds arguments to the local stack environment of a function. For instance, if
   * a function declares int foo(int a, int b), then a and b are ints and are
   * added to the local environment stack for later use. These cannot be
   * re-declared.
   * 
   * @param ctx         - ParserRuleContext from above method.
   * @param symbolTable - symboltable to add new environment to.
   * @param newScope    - new Scope of the environment.
   * @param args        - HashMap of IDs and datatypes from function declaration
   *                    (x->int, y->int)
   * @param id          - identifier of function.
   * @param retType     - return type of function.
   */
  private void addArgsToStack(ParserRuleContext ctx, SymbolTable symbolTable, LCSyntaxTree newScope,
      HashMap<String, String> args, String id, String retType) {
    Environment environment = new Environment();

    /* If the parameter map isn't empty, we can push them to the stack. */
    StringBuilder info = new StringBuilder(id + " " + retType + " (");
    if (!args.isEmpty()) {
      // Iterate through the arguments and declare the variables assigned onto the
      // local stack environment.
      for (String varID : args.keySet()) {
        String varDatatype = args.get(varID); // Gets the datatype.

        LCParameterDeclarationNode paramNode = new LCParameterDeclarationNode(ctx, varID, varDatatype);

        newScope.addChild(paramNode); // Creates the child
        environment.addSymbol(varID, new SymbolEntry("VAR", varDatatype)); // Adds it to the local symbol table.
        info.append(varDatatype + ",");
      }
      info.setCharAt(info.length() - 1, ')');
    } else {
      info.append(")");
    }

    super.setInfo(info.toString());
    symbolTable.addEnvironment(environment);
  }

  @Override
  public String toString() {
    return "void FNDEF " + this.getInfo();
  }
}
