package edu.joshuacrotts.littlec.icode;

/**
 * Intermediate code addresses. Addresses can indicate either global variables,
 * module-scope variables, local variables, function parameters, temporary
 * values, literals, or indexed locations (for arrays). When printed, names of
 * addresses are mangled according to the mangling rules in the assignment.
 */
public class ICAddress {
  private final static int ADDR_GLOBAL = 0, ADDR_LOCAL = 1, ADDR_PARAM = 2, ADDR_TEMP = 3, ADDR_LIT = 4, ADDR_STR = 5,
      ADDR_AIDX = 6, ADDR_MODULE = 7;
  private static int nextTemp = 1;
  private static int nextModule = 1;
  private static int nextString = 1;

  /**
   * A few pre-allocated literals for common constants.
   */
  public final static ICAddress CONST0 = newLitInt(0);
  public final static ICAddress CONST1 = newLitInt(1);
  public final static ICAddress CONST4 = newLitInt(4);

  private final int type;
  private int width;
  private int offset;
  private ICAddress base, idx;
  private String name;

  /**
   * Because *my* code pre-generates the addresses without use of this class, I
   * need to write a method that translates all intermediate code addresses into
   * ICAddresses.
   * 
   * @param icAddr
   * @param isLiteral
   */
  public ICAddress(String icAddr) {
    char fc = icAddr.charAt(0);
    this.name = icAddr;
    boolean isLiteral = (!icAddr.contains("l") && !icAddr.contains("@") && !icAddr.contains("t")
        && !icAddr.contains("p") && !icAddr.contains("g"));

    /* If we're not declaring a literal then it has to be a variable. */
    if (!isLiteral) {
      this.width = Character.getNumericValue(icAddr.charAt(1));

      /* Globals have no offset. */
      if (fc == 'g') {
        this.offset = 0;
      } else if (fc == 't') { /* Non-lit temps have underscores after the width. */
        this.offset = Integer.parseInt(icAddr.substring(icAddr.indexOf("_") + 1));
      } else { /* Locals have the at (@) symbol after the width. */
        this.offset = Integer.parseInt(icAddr.substring(icAddr.indexOf("@") + 1));
      }
    } else if (fc == 'S') { /* String variables have underscores after "0". */
      this.offset = Integer.parseInt(icAddr.substring(icAddr.indexOf("_") + 1));
    } else if (fc == 't') { /* Temporary literals are the same as the other temp. */
      this.width = Integer.parseInt(icAddr.substring(icAddr.indexOf("t") + 1, icAddr.indexOf("_")));
      this.offset = Integer.parseInt(icAddr.substring(icAddr.indexOf("_") + 1));
    } else {
      int l = Integer.parseInt(icAddr);
      this.width = l < 128 && l >= -127 ? 1 : 4;
    }

    // TODO Add array support.
    switch (fc) {
    case 'S':
      this.type = ADDR_STR;
      break;
    case 'p':
      this.type = ADDR_PARAM;
      break;
    case 't':
      this.type = ADDR_TEMP;
      break;
    case 'g':
      this.type = ADDR_GLOBAL;
      break;
    case 'l':
      this.type = ADDR_LOCAL;
      break;
    default:
      this.type = ADDR_LIT;
    }
  }

  public ICAddress(String icAddr, int width) {
    char fc = icAddr.charAt(0);
    this.name = icAddr;
    boolean isLiteral = (!icAddr.contains("l") && !icAddr.contains("@") && !icAddr.contains("t")
        && !icAddr.contains("p") && !icAddr.contains("g"));

    /* If we're not declaring a literal then it has to be a variable. */
    if (!isLiteral) {
      this.width = width;

      /* Globals have no offset. */
      if (fc == 'g') {
        this.offset = 0;
      } else if (fc == 't') { /* Non-lit temps have underscores after the width. */
        this.offset = Integer.parseInt(icAddr.substring(icAddr.indexOf("_") + 1));
      } else { /* Locals have the at (@) symbol after the width. */
        this.offset = Integer.parseInt(icAddr.substring(icAddr.indexOf("@") + 1));
      }
    } else if (fc == 'S') { /* String variables have underscores after "0". */
      this.offset = Integer.parseInt(icAddr.substring(icAddr.indexOf("_") + 1));
    } else if (fc == 't') { /* Temporary literals are the same as the other temp. */
      this.width = Integer.parseInt(icAddr.substring(icAddr.indexOf("t") + 1, icAddr.indexOf("_")));
      this.offset = Integer.parseInt(icAddr.substring(icAddr.indexOf("_") + 1));
    } else {
      int l = Integer.parseInt(icAddr);
      this.width = 4;// l < 128 && l >= -127 ? 1 : 4;
    }

    // TODO Add array support.
    switch (fc) {
    case 'S':
      this.type = ADDR_STR;
      break;
    case 'p':
      this.type = ADDR_PARAM;
      break;
    case 't':
      this.type = ADDR_TEMP;
      break;
    case 'g':
      this.type = ADDR_GLOBAL;
      break;
    case 'l':
      this.type = ADDR_LOCAL;
      break;
    default:
      this.type = ADDR_LIT;
    }
  }

  /**
   * Note that the constructor is private, so is only called internally. External
   * users should call one of the public methods below.
   * 
   * @param type the integer code for the type of this address
   */
  private ICAddress(int type) {
    this.type = type;
    this.offset = 0;
    this.width = 4;
    this.name = null;
  }

  /**
   * Create a global address. Since global variable names are significant, the
   * name is kept with this address and included inits mangled string
   * representation.
   * 
   * @param width size of the data type
   * @param name  name of the global variable
   * @return the ICAddress
   */
  public static ICAddress newGlobal(int width, String name) {
    ICAddress toRet = new ICAddress(ADDR_GLOBAL);
    toRet.width = width;
    toRet.name = ((width == -1) ? "gf_" : ("g" + width + "_")) + name;
    return toRet;
  }

  /**
   * Create a module-scope name, such a "static" global variable or function. This
   * works just like a global variable, except the mangled name starts with "m" to
   * let the code generation phase know that this name should\ not be exported in
   * the compiled module.
   * 
   * @param width size of the data type
   * @param name  name of the global variable
   * @return the ICAddress
   */
  public static ICAddress newModule(int width, String name) {
    ICAddress toRet = new ICAddress(ADDR_MODULE);
    if (name == null)
      name = "" + nextModule++;
    toRet.width = width;
    toRet.name = ((width == -1) ? "mf_" : ("m" + width + "_")) + name;
    return toRet;
  }

  /**
   * Create a local variable. Since local names are irrelevant in intermediate
   * code and later phases, a local variable address is identified by its offset
   * in the current stack frame.
   * 
   * @param offset location of this local variable in the current stack frame
   * @return the ICAddress
   */
  public static ICAddress newLocal(int width, int offset) {
    ICAddress toRet = new ICAddress(ADDR_LOCAL);
    toRet.width = width;
    toRet.offset = offset;
    toRet.name = "l" + width + "@" + offset;
    return toRet;
  }

  /**
   * Create a parameter variable. Since parameter names are irrelevant in
   * intermediate code and later phases, a parameter address is identified by its
   * byte offset in the parameter list.
   * 
   * @param offset location of this parameter in the current stack frame
   * @return the ICAddress
   */
  public static ICAddress newParam(int width, int offset) {
    ICAddress toRet = new ICAddress(ADDR_PARAM);
    toRet.offset = offset;
    toRet.width = width;
    toRet.name = "p" + width + "@" + offset;
    return toRet;
  }

  /**
   * Create a temporary variable. These have no externally-meaningful name, so a
   * new unique name is generated every time this constructor is called.
   * 
   * @param width size of the temporary variable (1 or 4)
   * @return the ICAddress
   */
  public static ICAddress newTemp(int width) {
    ICAddress toRet = new ICAddress(ADDR_TEMP);
    toRet.offset = nextTemp++;
    toRet.width = width;
    toRet.name = "t" + width + "_" + toRet.offset;
    return toRet;
  }

  /**
   * Create a new integer literal. This is an "address" that doesn't have\ an
   * lvalue, but is still referred to as an address in the documentation.
   * 
   * @param value the value for this literal
   * @return the ICAddress
   */
  public static ICAddress newLitInt(int value) {
    ICAddress toRet = new ICAddress(ADDR_LIT);
    toRet.offset = value;
    toRet.name = Integer.toString(value);
    return toRet;
  }

  /**
   * Create a new string literal. This allocates a new unique name which can be
   * used for the string table entry and to refer to this string.
   * 
   * @return the ICAddress
   */
  public static ICAddress newLitStr() {
    ICAddress toRet = new ICAddress(ADDR_STR);
    toRet.offset = nextString++;
    toRet.name = "S0_" + toRet.offset;
    return toRet;
  }

  /**
   * Create an indexed address.
   * 
   * @param width the width of each array element
   * @param base  the base address of the array (an ICAddress itself)
   * @param idx   the index into the array (an ICAddress itself)
   * @return the ICAddress
   */
  public static ICAddress newAIdx(int width, ICAddress base, ICAddress idx) {
    ICAddress toRet = new ICAddress(ADDR_AIDX);
    toRet.width = width;
    toRet.base = base;
    toRet.idx = idx;
    toRet.name = base + "[" + idx + "]";
    return toRet;
  }

  /**
   * Does this address represent a global variable?
   * 
   * @return true/false
   */
  public boolean isGlobal() {
    return type == ADDR_GLOBAL;
  }

  /**
   * Does this address represent a module-scope variable?
   * 
   * @return true/false
   */
  public boolean isModule() {
    return type == ADDR_MODULE;
  }

  /**
   * Does this address represent a literal/constant?
   * 
   * @return true/false
   */
  public boolean isLiteral() {
    return type == ADDR_LIT;
  }

  /**
   * Does this address represent a local variable?
   * 
   * @return true/false
   */
  public boolean isLocal() {
    return type == ADDR_LOCAL;
  }

  /**
   * Does this address represent a function parameter?
   * 
   * @return true/false
   */
  public boolean isParam() {
    return type == ADDR_PARAM;
  }

  /**
   * Does this address represent an array?
   * 
   * @return true/false
   */
  public boolean isArray() {
    return type == ADDR_AIDX;
  }

  /**
   * Does this address represent a string reference?
   * 
   * @return true/false
   */
  public boolean isString() {
    return type == ADDR_STR;
  }

  /**
   * Does this address represent a temporary variable?
   * 
   * @return true/false
   */
  public boolean isTemp() {
    return type == ADDR_TEMP;
  }

  /**
   * Does this address represent something potentially in RAM?
   * 
   * @return true/false
   */
  public boolean isMemBacked() {
    // Whether PARAMs are memory-backed is really target-specific, but
    // we'll assume all are. They at least have a canonical location.
    return (type == ADDR_TEMP) || (type == ADDR_GLOBAL) || (type == ADDR_MODULE) || (type == ADDR_LOCAL)
        || (type == ADDR_PARAM);
  }

  /**
   * For an array, get the base address.
   * 
   * @return the base address (another ICAddress)
   */
  public ICAddress getARef() {
    return base;
  }

  /**
   * For an array, get the index.
   * 
   * @return the index (another ICAddress)
   */
  public ICAddress getAIdx() {
    return idx;
  }

  /**
   * Get the offset associated with this address
   * 
   * @return the integer offset into its area of memory
   */
  public int getOffset() {
    return offset;
  }

  /**
   * Get the width of this address. Typically 1 or 4, but remember that arrays
   * have "width" 0.
   * 
   * @return the width
   */
  public int getWidth() {
    return width;
  }

  public void setWidth(int w) {
    this.width = w;
  }

  /**
   * Really the same as "toString' - it just seems natural to have this method
   * too.
   * 
   * @return the string representation of this address
   */
  public String getName() {
    return name;
  }

  /**
   * Convert the address to a string, incorporating the name mangling rules.
   * 
   * @return the string representation
   */
  @Override
  public String toString() {
    return name;
  }
}