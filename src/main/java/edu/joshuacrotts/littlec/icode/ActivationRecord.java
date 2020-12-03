package edu.joshuacrotts.littlec.icode;

import java.util.ArrayList;
import java.util.HashMap;

import edu.joshuacrotts.littlec.main.LCUtilities;

/**
 * An ActivationRecord keeps track of the number of local variables, global
 * variables, parameters, and string declarations there are in one block of
 * code. The String and global variables, however, are shared across all
 * ActivationRecords because they should not change (as they are declared in the
 * "global" AR/Environment; the one with the widest scope).
 * 
 * @author Joshua Crotts
 */
public class ActivationRecord {

  /** Compiler string-generated ID to string array of bytes. */
  private static final ArrayList<StringEntry> STRING_VARS = new ArrayList<>();

  /** ID -> Global variable ID. */
  private static final HashMap<String, String> GLOBAL_VARS = new HashMap<>();

  /** Keeps track of how many labels we have stored. */
  private static int labelCount = 0;

  /** ID -> Local Variable ID. */
  private final HashMap<String, String> LOCAL_VARS;

  /** ID -> Generated name for parameter. */
  private final HashMap<String, String> PARAM_VARS;

  /** ID -> Compiler generated name. */
  private int tempVars = 0;

  /** Tracks the current address offset for local space. */
  private int lAddressOffset = 0;

  /** Tracks the current address offset for parameters. */
  private int pAddressOffset = 0;

  public ActivationRecord() {
    this.LOCAL_VARS = new HashMap<>();
    this.PARAM_VARS = new HashMap<>();
  }

  /**
   * Increments the current label count by 1 and returns a new label with this new
   * value.
   * 
   * @param void.
   * 
   * @return new label.
   */
  public static String newLabel() {
    labelCount++;
    return "L" + labelCount;
  }

  /**
   * Adds a string (literal or char[]) to the string table in the activation
   * record.
   * 
   * @param s - string to add.
   * 
   * @return compiler-generated name of string.
   */
  public String addString(String s) {
    String compilerID = "S0_" + (ActivationRecord.STRING_VARS.size() + 1);
    StringEntry se = new StringEntry(s, compilerID);

    ActivationRecord.STRING_VARS.add(se);
    return compilerID;
  }

  /**
   * Adds a local variable declaration to the activation record. This method just
   * calls the addLocalArray function and passes 0 for the array size which is
   * treated differently internally.
   * 
   * @param id    - identifier of array.
   * @param width - size of each datatype
   * 
   * @throws IllegalArgumentException if width is not 0 or is not 1 or is not 4.
   * 
   * @return compiler-generated name for local variable.
   */
  public String addLocalVariable(String id, int width) {
    return this.addLocalArray(id, width, 0);
  }

  /**
   * Adds a local array declaration to the activation record.
   * 
   * @param id        - identifier of array.
   * @param width     - size of each datatype
   * @param arraySize - amount of elements stored
   * 
   * @throws IllegalArgumentException if width is not 0 or is not 1 or is not 4.
   * 
   * @return compiler-generated name for local array.
   */
  public String addLocalArray(String id, int width, int arraySize) {
    if (width != 0 && width != 1 && width != 4) {
      throw new IllegalArgumentException("cannot have non-byte or non-word width.");
    }

    if (width == 4) {
      this.lAddressOffset = LCUtilities.getNextAddress(this.lAddressOffset);
    }

    String localID = "l" + width + "@" + this.lAddressOffset;
    this.lAddressOffset += arraySize + ((width == 0) ? 4 : width);
    this.LOCAL_VARS.put(id, localID);

    return localID;
  }

  /**
   * Adds a global variable to the activation record.
   * 
   * @param id    - identifier of variable.
   * @param width - width of variable (type).
   * 
   * @throws IllegalArgumentException if width is not 0 or it's not 1 or it's not
   *                                  4.
   * 
   * @return compiler-generated name for parameter.
   */
  public String addGlobalVariable(String id, int width) {
    if (width != 0 && width != 1 && width != 4) {
      throw new IllegalArgumentException("cannot have non-byte or non-word width for global variable.");
    }

    String globalID = "g" + width + "_" + id;
    ActivationRecord.GLOBAL_VARS.put(id, globalID);

    return globalID;
  }

  /**
   * Adds a parameter variable to the activation record.
   * 
   * @param id    - identifier of variable.
   * @param width - width of variable (type).
   * 
   * @throws IllegalArgumentException if width is not 0 or it's not 1 or it's not
   *                                  4.
   * 
   * @return compiler-generated name for parameter.
   */
  public String addParameterVariable(String id, int width) {
    if (width != 0 && width != 1 && width != 4) {
      throw new IllegalArgumentException("cannot have non-byte or non-word width for parameter variable.");
    }

    if (width == 4) {
      this.pAddressOffset = LCUtilities.getNextAddress(this.pAddressOffset);
    }

    String paramID = "p" + width + "@" + this.pAddressOffset;
    this.pAddressOffset += width;
    this.PARAM_VARS.put(id, paramID);

    return paramID;
  }

  /**
   * Adds a temporary variable to the activation record.
   * 
   * @param width - width of variable to store.
   * 
   * @throws IllegalArgumentException if width is not 1 or is not 4.
   * 
   * @return compiler-generated ID.
   */
  public String addTemporaryVariable(int width) {
    if (width != 1 && width != 4) {
      throw new IllegalArgumentException("cannot have non-byte or non-word width for temporary variable.");
    }

    String tempID = "t" + width + "_" + (++this.tempVars);

    return tempID;
  }

  /**
   * Checks to see if the activation record has a local variable. If it exists,
   * the compiler name is returned. Null otherwise.
   * 
   * @param id - identifier of local variable (identifier; not compiler-generated
   *           name).
   * @return compiler-generated name if in map, null otherwise.
   */
  public String getLocalVariable(String id) {
    return this.LOCAL_VARS.get(id);
  }

  /**
   * Checks to see if the activation record has a parameter variable. If it
   * exists, the compiler name is returned. Null otherwise.
   * 
   * @param id - identifier of parameter variable (identifier; not
   *           compiler-generated name).
   * @return compiler-generated name if in map, null otherwise.
   */
  public String getParameterVariable(String id) {
    return this.PARAM_VARS.get(id);
  }

  /**
   * Checks to see if the global variable map contains the identifier. If it
   * exists, the compiler name is returned. Null otherwise.
   * 
   * @param id - identifier of global variable (identifier; not compiler-generated
   *           name).
   * @return compiler-generated name if in map, null otherwise.
   */
  public String getGlobalVariable(String id) {
    return ActivationRecord.GLOBAL_VARS.get(id);
  }

  /**
   * Returns the current string table.
   * 
   * @param void.
   * 
   * @return string table.
   */
  public ArrayList<StringEntry> getStringTable() {
    return ActivationRecord.STRING_VARS;
  }

  /**
   * Cleans up the stacks used in a static context. This is to prevent JUnit test
   * failures.
   * 
   * @param void.
   * 
   * @return void.
   */
  public void cleanup() {
    ActivationRecord.GLOBAL_VARS.clear();
    ActivationRecord.STRING_VARS.clear();
    labelCount = 0;
  }

  /**
   * Returns the space taken up by the local variables in the activation record.
   * 
   * @param void.
   * 
   * @return address offset. If we have no local variables, return 0.
   */
  public int getLocalSpace() {
    if (this.LOCAL_VARS.isEmpty()) {
      return 0;
    }

    return this.lAddressOffset;
  }
}