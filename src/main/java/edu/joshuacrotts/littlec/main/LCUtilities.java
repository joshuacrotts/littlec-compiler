package edu.joshuacrotts.littlec.main;

import org.antlr.v4.runtime.tree.TerminalNode;

import edu.joshuacrotts.littlec.syntaxtree.LCSyntaxTree;

/**
 * This class has utilities and static functions for use in other classes.
 * 
 * @author Joshua Crotts
 */
public class LCUtilities {

  /**
   * Returns the char representation of a string. The string should have one
   * quote, followed by a char, then another single-quote.
   *
   * @param characterStr single-quote (') followed by any char (a, b, \n),
   *                     followed by single-quote ('). Ex: 'a', 'b', '\n', '\0'
   *
   * @return char from the string. If the character represents an invalid escape
   *         char, then we return the character itself (e.g. \t returns 't').
   */
  public static char getCharFromString(String characterStr) {
    int beginQuote = characterStr.indexOf("'") + 1;
    int endQuote = characterStr.lastIndexOf("'");
    characterStr = characterStr.substring(beginQuote, endQuote);
    characterStr = LCUtilities.escapeString(characterStr);

    return characterStr.length() == 1 ? characterStr.charAt(0) : characterStr.charAt(1);
  }

  /**
   * Given a string, returns the string with all escape characters (in LittleC)
   * properly escaped. This is useful if one is reading in a stream of chars from
   * the console and has to insert the correct char (this is also flexible to add
   * other escape chars).
   *
   * @param str
   *
   * @return string of properly-escaped characters.
   */
  public static String escapeString(String str) {
    str = str.replaceAll("\\\\n", "\n");
    str = str.replaceAll("\\\\0", "\0");
    str = str.replaceAll("\\\\t", "\t");
    str = str.replaceAll("\\\\b", "\b");
    str = str.replaceAll("\\\\r", "\r");

    return str;
  }

  /**
   * Determines the "castability" between two data types. Chars and ints are
   * interchangable, and array types can go from a declaration to a reference, but
   * not the other way around.
   * 
   * @param from
   * @param to
   * @return true if we can cast from type to "to" type, false otherwise.
   */
  public static boolean isCastable(String from, String to) {
    if ((from.equals("char") && to.equals("int")) || (from.equals("int") && to.equals("char"))
        || (from.equals("char") && to.equals("float")) || (from.equals("float") && to.equals("char"))
        || (from.equals("int") && to.equals("float")) || (from.equals("float") && to.equals("int"))) {
      return true;
    }

    // Array declaration (int a[20]) to array reference (int[]).
    if (from.matches(".+\\[[0-9]+\\]") && to.endsWith("[]")) {
      String arrType1 = LCUtilities.getArrayType(from);
      String arrType2 = LCUtilities.getArrayType(to);

      return arrType1.equals(arrType2);
    }

    return false;
  }

  /**
   * Determines if a String variable type (stored in the symbol table) is an array
   * or not. This does not differentiate between array refs and array
   * declarations.
   * 
   * @param varType - string variable type.
   * @return true if varType is an array reference or declaration; false
   *         otherwise.
   */
  public static boolean isTypeArray(String varType) {
    return varType.contains("[]") || varType.indexOf("[") < varType.indexOf("]");
  }

  /**
   * Returns the integer representation of a string in hex, binary, or decimal
   * form.
   * 
   * ** A call to this method should most likely be prefaced with a call to
   * isValidIntLiteral to make sure it is valid. **
   * 
   * Because Integer.decode() doesn't work on hex values where the most
   * significant bit (the sign bit) is toggled, we have to convert it to a long,
   * then use its .intValue() method. Ugh.
   * 
   * A NumberFormatException is thrown by the Integer.decode call if the string
   * passed is not a valid, parsable integer.
   * 
   * @param intVal - string to parse.
   *
   * @throws IllegalArgumentException if the integer is not valid.
   * 
   * @return integer representation of the literal.
   */
  public static int getDecodedIntLiteral(String intVal) {
    return Long.decode(intVal).intValue();
  }

  /**
   * Determines if the string is a valid, parsable integer literal. The string may
   * have a leading 0x, 0b, or standard decimal.
   * 
   * @param intVal - string to parse.
   * 
   * @return true if the string represents a valid 32-bit integer, false
   *         otherwise.
   */
  public static boolean isValidIntLiteral(String intVal) {
    try {
      int v = Long.decode(intVal).intValue();
      if (v < Integer.MIN_VALUE || v > Integer.MAX_VALUE) {
        return false;
      }
    } catch (NumberFormatException ex) {
      return false;
    }

    return true;
  }

  /**
   * Determines if all flags passed in args are disabled in the LCSyntaxTree
   * flags.
   * 
   * @param tree
   * @param args - var args of integer flag values.
   * @return true if all flags passed are set to 0, false otherwise.
   */
  public static boolean isEveryFlagDisabled(LCSyntaxTree tree, int... args) {
    int result = 0;
    for (int flag : args) {
      result ^= flag;
    }
    return (tree.getFlags() & result) == 0;
  }

  /**
   * Determines if the operator passed in is a relational operator or not.
   * 
   * @param op - string.
   * @return true if operator is relational (<, <=, >, >=, ==, !=). False
   *         otherwise.
   */
  public static boolean isRelationalOp(String op) {
    return (op.equals("<") || op.equals("<=") || op.equals(">") || op.equals(">=") || op.equals("!=")
        || op.equals("=="));
  }

  /**
   * Determines if the operator passed in is a logical operator or not.
   * 
   * @param op - string.
   * @return true if operator is logical (&&, ||). False otherwise.
   */
  public static boolean isComparisonOp(String op) {
    return op.equals("&&") || op.equals("||");
  }

  /**
   * Computes the opposite relational operator of the supplied operator.
   * 
   * @param relOp - string.
   * @return relational operator that is the opposite of the one passed (ex.
   *         opposite of != is ==).
   */
  public static String getOppositeRelOp(String relOp) {
    switch (relOp) {
    case "<":
      return ">=";
    case ">":
      return "<=";
    case "<=":
      return ">";
    case ">=":
      return "<";
    case "==":
      return "!=";
    case "!=":
      return "==";
    default:
      throw new IllegalArgumentException("Cannot return opposite of " + relOp);
    }
  }

  /**
   * Calculates the size in bytes of a data type.
   * 
   * @param type
   * @return
   */
  public static int getDataWidth(String type) {
    switch (type) {
    case "int[]":
    case "char[]":
    case "int":
      return 4;
    case "char":
      return 1;
    }

    /* If we're at this point, the type has to be an array declaration. */
    int lBracketPos = type.indexOf("[");
    int rBracketPos = type.indexOf("]");
    int arraySize = Integer.parseInt(type.substring(lBracketPos + 1, rBracketPos));

    // Kind of a sloppy way to do it since I'm not using enums... maybe optimize for
    // last
    // stage.
    String coreType = type.contains("int") ? "int" : "char";

    return arraySize * getDataWidth(coreType);
  }

  /**
   * Given an array declaration (int a[5]), returns the number of elements that
   * the array can store. Note that this *does* assume a valid "type" is passed.
   * If this is not the case, an exception will be thrown.
   * 
   * @param arrayDeclaration - string of form (int/char id[num]).
   * 
   * @return number of elements in the array.
   */
  public static int getArraySize(String type) {
    int lBracketPos = type.indexOf("[");
    int rBracketPos = type.indexOf("]");
    int arraySize = 0;
    // If we're in an array reference, the logical size will be 0.
    try {
      arraySize = Integer.parseInt(type.substring(lBracketPos + 1, rBracketPos));
    } catch (Exception e) {
      throw new IllegalArgumentException("Cannot use " + type + " as an array declaration.");
    }

    return arraySize;
  }

  /**
   * Returns the byte string representation of an ASCII string. A byte string in
   * this context is the "assembly initialization" of the string, meaning that
   * there is a ".db" prefix, followed by the ascii values of the string. For
   * instance, "Hi there" returns ".db 72, 105, 32, 116, 104, 101, 114, 101, 0"
   *
   * Each byte string has the null-terminator appended.
   * 
   * @warning Make sure that all strings are *only* strings (i.e. without
   *          quotations and properly escaped).
   * 
   * @param str - properly-escaped string with no quotes.
   * 
   * @return byte string of string.
   */
  public static String getByteString(String str) {
    StringBuilder sb = new StringBuilder(".db ");
    for (int i = 0; i < str.length(); i++) {
      sb.append((int) str.charAt(i));
      sb.append(", ");
    }
    sb.append("0");

    return sb.toString();
  }

  /**
   * Returns the data type used for an array reference. This assumes that the call
   * is being used on a valid string.
   * 
   * @param arr - string declaration of array type (int[], char[]).
   * @return type used for array ref.
   */
  public static String getArrayType(String arr) {
    return arr.substring(0, arr.indexOf("["));
  }

  /**
   * Returns whether or not the passed string is a literal or not.
   * 
   * @param lit string.
   * 
   * @return true if we can cast the number to an integer, false otherwise.
   */
  public static boolean isNumericLit(String lit) {
    try {
      Integer.parseInt(lit);
    } catch (NumberFormatException ex) {
      return false;
    }
    return true;
  }

  /**
   * Computes the next address in the stack. Since addresses in x86 are 32-bit, we
   * increment the address until it's a multiple of four bytes. This is useful for
   * when chars and ints are declared interchangably since ints have to be in an
   * address that's a multiple of four.
   * 
   * @param addr - current address.
   * @return next available address.
   */
  public static int getNextAddress(int addr) {
    while (addr % 4 != 0) {
      addr++;
    }

    return addr;
  }

  /**
   * Computes the next address in the stack. Since the stack pointer in MIPS is
   * required to be aligned on 8-byte boundaries, we may need to round up the
   * address.
   * 
   * @param addr - current address.
   * @return next available address.
   */
  public static int getNextMIPSAddress(int addr) {
    while (addr % 8 != 0) {
      addr++;
    }

    return addr;
  }

  /**
   * Returns the string representation of the storage classes.
   * 
   * @param externNode
   * @param staticNode
   * @return string representation of storage class.
   */
  public static String getStorageClassType(TerminalNode externNode, TerminalNode staticNode) {
    if (externNode != null) {
      return "extern";
    } else if (staticNode != null) {
      return "static";
    } else {
      return "auto";
    }
  }

  /**
   * Recursively checks if a LCSyntaxTree has return statements in all blocks.
   * 
   * If the tree has greater than three children, we recurse on the last child. If
   * the tree has three children, it's an if() with the else portion. If the tree
   * has less than three children, we recurse on the last child.
   * 
   * @param lastChild
   * 
   * @return true if all blocks have returns, false otherwise.
   */
  public static boolean hasReturn(LCSyntaxTree lastChild) {
    int size = lastChild.getChildren().size();

    // One base case is if we start with ONLY an if.
    // If we do, we check for the existence of a return. If there are only two
    // children,
    // then the invariant is that this HAS to be a return statement.
    //
    // The only other case where we immediately return false is if we have no
    // children.
    if ((lastChild.getLabel().equals("IF") && !lastChild.getChildren().get(size - 1).getLabel().equals("RETURN")
        && size == 2) || lastChild.getChildren().isEmpty()) {
      return false;
    } else {
      // Check if the current last child IS a return statement, or if its last child
      // is a return (this
      // can happen when the last child is an else statement (NOT else if)).
      if (lastChild.getLabel().equals("RETURN")) {
        return true;
      } else {
        if (size != 3) {
          // If we have more than three children, just recurse on the last child.
          return hasReturn(lastChild.getChildren().get(size - 1));
        } else {
          // Two possible times this could be here:
          // 1. When we're in an if, else block.
          // 2. When we're in a new BLOCK {...} of data.
          //
          // If the latter, then we only need to check the last node in the tree
          // since earlier stuff may be declarations or other arbitrary things.
          // We check if the second block is a sequence for this very reason.
          // The first child is a void when we're not in a comparison (i.e. we're
          // in a block, so we only need to check the last statement.
          if (lastChild.getChildren().get(size - 1).getLabel().equals("RETURN")
              && isValidSecondChild(lastChild.getChildren().get(size - 2).getLabel())
              && lastChild.getChildren().get(size - 3).getType().equals("void")) {
            return true;
          } else {
            return hasReturn(lastChild.getChildren().get(size - 1)) && hasReturn(lastChild.getChildren().get(size - 2));
          }
        }
      }
    }
  }

  /**
   * Returns whether or not the second child in a SEQ tree is valid or not. This
   * is only used with the above method.
   *
   * @param str
   * @return
   */
  private static boolean isValidSecondChild(String str) {
    return str.equals("SEQ") || str.equals("WHILE");
  }
}
