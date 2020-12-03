package edu.joshuacrotts.littlec.syntaxtree;

import java.util.LinkedList;

import org.antlr.v4.runtime.ParserRuleContext;

import edu.joshuacrotts.littlec.icode.ICInhAttr;
import edu.joshuacrotts.littlec.icode.ICode;
import edu.joshuacrotts.littlec.main.LCUtilities;
import edu.joshuacrotts.littlec.main.SymbolTable;

public class LCFunctionCallNode extends LCSyntaxTree {

  /* Identifier of function. */
  private String id;

  /**
   * Creates a function call node. Each parameter type is type-checked against the
   * declaration stored in the symbol table, or the forward declaration if it
   * exists.
   * 
   * Child (0-many): Arguments passed to the function. Reported as () if zero
   * children.
   * 
   * @param ctx
   * @param symbolTable
   * @param id
   * @param parameters
   */
  public LCFunctionCallNode(ParserRuleContext ctx, SymbolTable symbolTable, String id,
      LCFunctionArgsListNode parameters) {
    this.id = id;
    /* Get the definition parameters from the symbol table so we can check them. */
    LinkedList<LCSyntaxTree> fnDefArgs = (LinkedList<LCSyntaxTree>) symbolTable.getSymbolEntry(id).getInfoList();
    LinkedList<LCSyntaxTree> parametersList;

    /*
     * If we don't have a parameter list object defined, then we just give it a
     * blank list. That's what it's analogous to.
     */
    if (parameters != null) {
      parametersList = (LinkedList<LCSyntaxTree>) parameters.getParams();
    } else {
      parametersList = new LinkedList<>();
    }

    /*
     * If the two parameter declarations aren't the same size, then there's no point
     * of continuing.
     */
    if (fnDefArgs.size() != parametersList.size()) {
      this.printError(ctx, "function definition for " + id + " expects " + fnDefArgs.size()
          + " arguments, but was given " + parametersList.size() + ".");
      return;
    }

    /* Go through one by one and compare the types. */
    for (int i = 0; i < fnDefArgs.size(); i++) {
      /*
       * If we get to here and find a null value in the parameter list, we just bail
       * out.
       */
      if (fnDefArgs.get(i) == null || parametersList.get(i) == null) {
        this.printError(ctx, "parameter " + (i + 1) + " is null.");
        return;
      }

      String fnArg = fnDefArgs.get(i).getType();
      String param = parametersList.get(i).getType();

      if (!fnArg.equals(param)) {
        // If we can cast from one type ot another, try to.
        if (LCUtilities.isCastable(param, fnArg)) {
          LCTypeCastNode castingNode = new LCTypeCastNode(ctx, parametersList.get(i), fnArg);
          parametersList.set(i, castingNode);
        } else {
          this.printError(ctx, "function call: declaration for function " + id + " parameter " + (i + 1) + " expects "
              + fnArg + " but " + param + " was given.");
          return;
        }
      }
    }

    this.setLabel("FNCALL");
    this.setType(symbolTable.getSymbolEntry(id).getVarType());
    this.setInfo(id);

    /* If we actually have parameters, add them as children of this node. */
    if (parameters != null) {
      /* 0 or more children depending on the parameters. */
      for (LCSyntaxTree param : parameters.getParams()) {
        super.addChild(param);
      }
    } else {
      // If no parameters exist then we just add empty parenthesis.
      this.setInfo(id + " ()");
    }
  }

  /**
   * 
   */
  @Override
  public void genCode(ICInhAttr info) {
    if (super.isCalled)
      return;
    super.isCalled = true;

    int args = this.getChildren().size();

    // Push the arguments in reverse order.
    for (int i = args - 1; i >= 0; i--) {
      LCSyntaxTree param = this.getChildren().get(i);
      int width = LCUtilities.getDataWidth(param.getType());
      param.genCode(info);
      // Add the parameter to <op1> <op>
      ICode.quad.addLine("", info.ADDR, "", "param" + width);
    }

    // If the return type is non-void, we need to generate a new compiler temp
    // variable.
    if (!this.getType().equals("void")) {
      String tempVar = ICode.getTopAR().addTemporaryVariable(LCUtilities.getDataWidth(this.getType()));
      ICode.quad.addFunctionCall(tempVar, "gf_" + this.id, args);
      info.ADDR = tempVar;
    } else {
      // ...otherwise, we just write it on the next line.
      ICode.quad.addVoidFunctionCall("gf_" + this.id, args);
    }

    // If the fncall is embedded in an IF we need to display that.
    // The only way we can be here is if the method is non-void.
    if (info.TYPE.equals("IF_COND")) {
      ICode.quad.addLine("goto " + info.TRUE, info.ADDR, "1", "if==");
      ICode.quad.addLabel("goto " + info.FALSE);
    }
  }

  @Override
  public String toString() {
    return this.getType() + " " + this.getLabel() + " " + this.getInfo();
  }
}
