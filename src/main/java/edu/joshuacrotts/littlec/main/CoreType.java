package edu.joshuacrotts.littlec.main;

public class CoreType {

  /**
   * 
   */
  public static CoreType VOID = new CoreType("void", 0, 0);

  /**
   * 
   */
  public static CoreType INT = new CoreType("int", 2, 4);

  /**
   * 
   */
  public static CoreType CHAR = new CoreType("char", 1, 1);

  /**
   * 
   */
  public static CoreType FLOAT = new CoreType("float", 3, 4);

  /**
   * 
   */
  public static CoreType INT_ARRAY = new CoreType("int[]", 1, 0);

  /**
   * 
   */
  public static CoreType CHAR_ARRAY = new CoreType("char[]", 2, 0);

  /**
   * 
   */
  public static CoreType FLOAT_ARRAY = new CoreType("float[]", 3, 0);

  /**
   * 
   */
  private String type;

  /**
   * 
   */
  private int priority;

  /**
   * 
   */
  private int size;

  /**
   * 
   */
  private int width;

  public CoreType(CoreType coreType, int size) {
    this.type = coreType.type + "[" + size + "]";
    this.priority = coreType.priority;
    this.size = size;
  }

  private CoreType(String type, int priority, int width) {
    this.type = type;
    this.priority = priority;
    this.size = -1;
    this.width = width;
  }

  /**
   * 
   * @param type
   * @return
   */
  public static CoreType getTypeFromString(String type) {
    switch (type) {
    case "int":
      return CoreType.INT;
    case "float":
      return CoreType.FLOAT;
    case "char":
      return CoreType.CHAR;
    case "int[]":
      return CoreType.INT_ARRAY;
    case "char[]":
      return CoreType.CHAR_ARRAY;
    case "float[]":
      return CoreType.FLOAT_ARRAY;
    }
    return null;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof String) {
      throw new RuntimeException("cannot use string as coretype.");
    }
    
    CoreType oth = (CoreType) obj;
    return oth.type.equals(this.type) && oth.priority == this.priority && oth.size == this.size;
  }

  public boolean isArray() {
    return this.isArrayDecl() || this.isArrayRef();
  }

  public boolean isArrayRef() {
    return this.type.contains("[]");
  }

  public boolean isArrayDecl() {
    return this.type.matches("[.]+\\[\\d\\]");
  }

  public boolean isInt() {
    return this.type.equals("int");
  }

  public boolean isChar() {
    return this.type.equals("char");
  }

  public boolean isFloat() {
    return this.type.equals("float");
  }

  public String getICDataSpecifier() {
    switch (this.type) {
    case "int":
      return ".dw";
    case "float":
      return ".dw";
    case "char":
      return ".db";
    }
    return null;
  }

  public String getMIPSDataSpecifier() {
    switch (this.type) {
    case "int":
      return ".word";
    case "float":
      return ".float";
    case "char":
      return ".byte";
    }
    return null;
  }

  public String getType() {
    return this.type;
  }

  public int getPriority() {
    return this.priority;
  }

  public int getSize() {
    return this.size;
  }

  public int getWidth() {
    return this.width;
  }

  @Override
  public String toString() {
    return this.type;
  }
}
