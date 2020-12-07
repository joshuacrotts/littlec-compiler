package edu.joshuacrotts.littlec.icinterp;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;
import java.util.Vector;

/**
 * Interpreter for 3-address intermediate code, as defined for Phase 4 of the
 * CSC 439 compiler project.
 *
 * This is not great code, and it's not really documented, but it works
 * (hopefully!). No more time to test or clean up, because it needs to go out to
 * the class.
 *
 * @author Steve Tate
 * 
 * @modified Joshua Crotts
 */
public class ICInterp {
  private static void debugPrint(String m) {
//        System.out.println(m);
  }

  private Map<String, Integer> labels;
  private Vector<AddrSegment> segments;
  private Stack<Integer> segFreeList;
  private String[] lines;
  private int[] globals;
  private AddrSegment dSegment;
  private Scanner sin;

  public class AddrSegment {
    private byte[] storage;
    private int size;
    private int segID;

    private static final int SLACK = 64;

    public AddrSegment(int size) {
      if (size > 0x10000) {
        System.err.println("Internal simulator error: Requested " + size + " segment");
        System.exit(1);
      }
      if (!segFreeList.isEmpty()) {
        segID = segFreeList.pop();
        segments.set(segID, this);
      } else {
        segID = segments.size();
        if (segID >= 0x10000) {
          System.err.println("Internal simulator error: Too many segments");
          System.exit(1);
        }
        segments.add(this);
      }
      this.size = size;
      storage = new byte[(size == 0) ? 32 : size];
    }

    public int getID() {
      return segID;
    }

    public void delete() {
      segFreeList.push(segID);
      segments.set(segID, null);
    }

    public int growBy(int toAdd) {
      if (size + toAdd > storage.length) {
        storage = Arrays.copyOf(storage, size + toAdd + SLACK);
      }
      int newStart = size;
      size += toAdd;
      return getPtr(newStart);
    }

    public int getPtr(int offset) {
      if ((offset < 0) || (offset >= size)) {
        System.err.println("Internal sim error: Bad segment offset");
        System.exit(1);
      }
      return (segID << 16) + offset;
    }

    public int getExtent() {
      return (segID << 16) + size;
    }

    public int getInt(int offset) {
      int val = 0;
      if (offset + 3 < size) {
        val = ((int) (storage[offset]) & 0xff) | (((int) (storage[offset + 1]) & 0xff) << 8)
            | (((int) (storage[offset + 2]) & 0xff) << 16) | (((int) (storage[offset + 3]) & 0xff) << 24);
      } else {
        System.err.println("Out of bounds memory access (getInt) - this should never happen.");
        System.exit(1);
      }
      return val;
    }

    public void setInt(int offset, int val) {
      if (offset + 3 < size) {
        storage[offset] = (byte) (val & 0xff);
        storage[offset + 1] = (byte) ((val >> 8) & 0xff);
        storage[offset + 2] = (byte) ((val >> 16) & 0xff);
        storage[offset + 3] = (byte) ((val >> 24) & 0xff);
      } else {
        System.err.println("Out of bounds memory access (setInt) - this should never happen.");
        System.exit(1);
      }
    }

    public byte getByte(int offset) {
      byte rVal = 0;
      if (offset < size) {
        rVal = storage[offset];
      } else {
        System.err.println("Out of bounds memory access (getByte) - this should never happen.");
        System.exit(1);
      }
      return rVal;
    }

    public void setByte(int offset, byte val) {
      if (offset < size) {
        storage[offset] = val;
      } else {
        System.err.println("Out of bounds memory access (setByte) - this should never happen.");
        System.exit(1);
      }
    }

    public SimValue getVal(int width, int offset) {
      if (width == 1)
        return new SimValue(1, getByte(offset));
      else
        return new SimValue(4, getInt(offset));
    }

    public void setVal(int offset, SimValue val) {
      if (val.width == 1)
        setByte(offset, val.cVal);
      else
        setInt(offset, val.iVal);
    }
  }

  public AddrSegment ptrToSeg(int ptr) {
    int segID = (ptr >> 16) & 0xffff;
    if (segID >= segments.size()) {
      System.err.println("Internal sim error: Bad segment number");
      System.exit(1);
    }
    return segments.get(segID);
  }

  public int ptrGetInt(int ptr, int offset) {
    return ptrToSeg(ptr).getInt((ptr & 0xffff) + offset);
  }

  public void ptrSetInt(int ptr, int offset, int val) {
    ptrToSeg(ptr).setInt((ptr & 0xffff) + offset, val);
  }

  public byte ptrGetByte(int ptr, int offset) {
    return ptrToSeg(ptr).getByte((ptr & 0xffff) + offset);
  }

  public void ptrSetByte(int ptr, int offset, byte val) {
    ptrToSeg(ptr).setByte((ptr & 0xffff) + offset, val);
  }

  public void ptrSetVal(int ptr, SimValue val) {
    if (val.width == 1)
      ptrSetByte(ptr, 0, val.cVal);
    else
      ptrSetInt(ptr, 0, val.iVal);
  }

  public static class SimValue {
    private enum SimType {
      CHAR,
      INT,
      FLOAT;
    }
    public static SimValue errVal = new SimValue(4, 999999999);
    public SimType type;
    public int width;
    public int iVal;
    public float fVal;
    public byte cVal;

    public SimValue(int width, int val) {
      this.type = width==1?SimType.CHAR : SimType.INT;
      this.width = width;
      iVal = val;
      cVal = (byte) val;
    }

    public SimValue(int width, float val) {
      this.type = SimType.FLOAT;
      this.width = width;
      fVal = val;
    }

    public int getAsI() {
      if (this.type == SimType.INT)
        return iVal;
      else
        return cVal;
    }

    public int getAsC() {
      if (this.type == SimType.CHAR) {
        return cVal;
      }
      throw new RuntimeException("Cannot return non-byte width val as char.");
    }

    public float getAsF() {
      if (this.type == SimType.FLOAT) {
        return this.fVal;
      }
      throw new RuntimeException("Cannot return non-word width val as float.");
    }
  }

  public static int getTWidth(String name) {
    switch (name.charAt(0)) {
    case 't':
    case 'l':
    case 'p':
    case 'g':
    case 'm':
      char c = name.charAt(1);
      if (!Character.isDigit(c))
        return 0;
      else
        return c - '0';
    default:
      return 0;
    }
  }

  public int makeGlobalVar(int size) {
    AddrSegment thisSeg = new AddrSegment(size);
    return thisSeg.getPtr(0);
  }

  public int makeGlobalVarArray(int elemSize, int size) {
    AddrSegment thisSeg = new AddrSegment(4 + elemSize * size);
    thisSeg.setInt(0, size);
    return thisSeg.getPtr(0);
  }

  public int makeGlobalVarString(String val) {
    Vector<Byte> chars = new Vector<>();
    int pos = 0;
    while (pos < val.length()) {
      char c = val.charAt(pos++);
      if ((c == '\\') && (pos < val.length())) {
        char c2 = val.charAt(pos++);
        switch (c2) {
        // Properly escape the characters.
        case 'n':
          c = '\n';
          break;
        case '0':
          c = '\0';
          break;
        case 't':
          c = '\t';
          break;
        case 'b':
          c = '\b';
          break;
        case 'r':
          c = '\r';
          break;
        default:
          c = c2;
          break;
        }
      }
      chars.add((byte) c);
    }

    int ptr = makeGlobalVarArray(1, chars.size());
    AddrSegment thisSeg = ptrToSeg(ptr);

    for (int i = 0; i < chars.size(); i++) {
      thisSeg.setByte(4 + i, chars.elementAt(i));
    }
    return ptr;
  }

  private class LocalEnv {
    private AddrSegment lSegment;
    private AddrSegment pSegment;
    private Map<String, SimValue> tempVars;
    private int currLine;
    private Stack<SimValue> argStack;
    private SimValue retVal;

    public LocalEnv(int startLine, List<SimValue> paramList) {
      this.pSegment = new AddrSegment(4 * paramList.size());
      for (int i = 0; i < paramList.size(); i++) {
        pSegment.setVal(4 * i, paramList.get(i));
      }
      this.tempVars = new HashMap<>();
      this.currLine = startLine;
      this.argStack = new Stack<>();
      this.retVal = null;
    }

    public void freeSpace() {
      if (lSegment != null) {
        lSegment.delete();
        lSegment = null;
      }
      if (pSegment != null) {
        pSegment.delete();
        pSegment = null;
      }
    }

    public SimValue getRetVal() {
      return (this.retVal == null) ? SimValue.errVal : this.retVal;
    }

    public SimValue getVal(int tWidth, String name) {
      if (name.startsWith("l")) {
        return lSegment.getVal(getTWidth(name), Integer.parseInt(name.substring(3)));
      } else if (name.startsWith("p")) {
        return pSegment.getVal(getTWidth(name), Integer.parseInt(name.substring(3)));
      } else if (name.startsWith("g") || (name.startsWith("m"))) {
        int ptr = globals[labels.get(name)];
        return ptrToSeg(ptr).getVal(getTWidth(name), 0);
      } else if (name.startsWith("S")) {
        return new SimValue(4, globals[labels.get(name)]);
      } else if (name.startsWith("t")) {
        SimValue val = tempVars.get(name);
        if (val == null)
          return SimValue.errVal;
        else
          return val;
      } else if (Character.isDigit(name.charAt(0)) || !Character.isAlphabetic(name.charAt(0))) {
        return new SimValue(tWidth, Integer.parseInt(name));
      }
      return SimValue.errVal;
    }

    public int getAddr(String name) {
      if (name.startsWith("l")) {
        return lSegment.getPtr(Integer.parseInt(name.substring(3)));
      } else if (name.startsWith("p")) {
        return pSegment.getPtr(Integer.parseInt(name.substring(3)));
      } else if (name.startsWith("g")) {
        return globals[labels.get(name)];
      } else if (name.startsWith("S")) {
        return globals[labels.get(name)];
      }
      return 0;
    }

    public void setVal(String name, SimValue val) {
      if (name.startsWith("l")) {
        lSegment.setVal(Integer.parseInt(name.substring(3)), val);
      } else if (name.startsWith("p")) {
        pSegment.setVal(Integer.parseInt(name.substring(3)), val);
      } else if (name.startsWith("g") || name.startsWith("m")) {
        int ptr = globals[labels.get(name)];
        ptrSetVal(ptr, val);
      } else if (name.startsWith("t")) {
        tempVars.put(name, val);
      }
    }

    public SimValue calcVal(int tWidth, String[] parts, int si) {
      SimValue val1;
      SimValue val2;
      int rval;

      // Check for special values/operations first
      if (parts[si].equals("narrow")) {
        val1 = getVal(tWidth, parts[si + 1]);
        return new SimValue(1, val1.getAsI());
      } else if (parts[si].equals("widen")) {
        val1 = getVal(tWidth, parts[si + 1]);
        return new SimValue(4, val1.getAsI());
      } else if (parts[si].substring(0, 1).matches("[A-Za-z0-9_]")) {
        val1 = getVal(tWidth, parts[si]);
        if (si + 2 >= parts.length)
          return val1;
        // Must be a binary op
        val2 = getVal(tWidth, parts[si + 2]);
        if ((tWidth == 1) || (val1.width == 1) && (val2.width == 1)) {
          byte cval = 111;
          // TODO: Break this up...
          if (parts[si + 1].equals("-"))
            return new SimValue(1, (byte) (val1.cVal - val2.cVal));
          else if (parts[si + 1].equals("+"))
            return new SimValue(1, (byte) (val1.cVal + val2.cVal));
          else if (parts[si + 1].equals("*"))
            return new SimValue(1, (byte) (val1.cVal * val2.cVal));
          else if (parts[si + 1].equals("/"))
            return new SimValue(1, (byte) (val1.cVal / val2.cVal));
          else if (parts[si + 1].equals("%"))
            return new SimValue(1, (byte) (val1.cVal % val2.cVal));
          else if (parts[si + 1].equals("^"))
            return new SimValue(1, (byte) (val1.cVal ^ val2.cVal));
          else if (parts[si + 1].equals("^"))
            return new SimValue(1, (byte) (val1.cVal ^ val2.cVal));
          else if (parts[si + 1].equals("&"))
            return new SimValue(1, (byte) (val1.cVal & val2.cVal));
          else if (parts[si + 1].equals("|"))
            return new SimValue(1, (byte) (val1.cVal | val2.cVal));
          else if (parts[si + 1].equals("<<"))
            return new SimValue(1, (byte) (val1.cVal << val2.cVal));
          else if (parts[si + 1].equals(">>"))
            return new SimValue(1, (byte) (val1.cVal >> val2.cVal));
          else if (parts[si + 1].equals("<"))
            return new SimValue(4, (val1.cVal < val2.cVal) ? 1 : 0);
          else if (parts[si + 1].equals("<="))
            return new SimValue(4, (val1.cVal <= val2.cVal) ? 1 : 0);
          else if (parts[si + 1].equals(">"))
            return new SimValue(4, (val1.cVal > val2.cVal) ? 1 : 0);
          else if (parts[si + 1].equals(">="))
            return new SimValue(4, (val1.cVal >= val2.cVal) ? 1 : 0);
          else if (parts[si + 1].equals("=="))
            return new SimValue(4, (val1.cVal == val2.cVal) ? 1 : 0);
          else if (parts[si + 1].equals("!="))
            return new SimValue(4, (val1.cVal != val2.cVal) ? 1 : 0);
          else if (parts[si + 1].equals("ldidx1"))
            return new SimValue(1, ptrGetByte(val1.iVal, 4 + val2.iVal));
          else
            return SimValue.errVal;
        } else {
          if (parts[si + 1].equals("-"))
            return new SimValue(4, val1.iVal - val2.iVal);
          else if (parts[si + 1].equals("+"))
            return new SimValue(4, val1.iVal + val2.iVal);
          else if (parts[si + 1].equals("*"))
            return new SimValue(4, val1.iVal * val2.iVal);
          else if (parts[si + 1].equals("/"))
            return new SimValue(4, val1.iVal / val2.iVal);
          else if (parts[si + 1].equals("%"))
            return new SimValue(4, val1.iVal % val2.iVal);
          else if (parts[si + 1].equals("^"))
            return new SimValue(4, val1.iVal ^ val2.iVal);
          else if (parts[si + 1].equals("&"))
            return new SimValue(4, val1.iVal & val2.iVal);
          else if (parts[si + 1].equals("|"))
            return new SimValue(4, val1.iVal | val2.iVal);
          else if (parts[si + 1].equals("<<"))
            return new SimValue(4, val1.iVal << val2.iVal);
          else if (parts[si + 1].equals(">>"))
            return new SimValue(4, val1.iVal >> val2.iVal);
          else if (parts[si + 1].equals("<"))
            return new SimValue(4, (val1.iVal < val2.iVal) ? 1 : 0);
          else if (parts[si + 1].equals("<="))
            return new SimValue(4, (val1.iVal <= val2.iVal) ? 1 : 0);
          else if (parts[si + 1].equals(">"))
            return new SimValue(4, (val1.iVal > val2.iVal) ? 1 : 0);
          else if (parts[si + 1].equals(">="))
            return new SimValue(4, (val1.iVal >= val2.iVal) ? 1 : 0);
          else if (parts[si + 1].equals("=="))
            return new SimValue(4, (val1.iVal == val2.iVal) ? 1 : 0);
          else if (parts[si + 1].equals("!="))
            return new SimValue(4, (val1.iVal != val2.iVal) ? 1 : 0);
          else if (parts[si + 1].equals("ldidx4")) {
            return new SimValue(4, ptrGetInt(val1.iVal, 4 + 4 * val2.iVal));
          } else if (parts[si + 1].equals("ldidx1")) {
            return new SimValue(1, ptrGetByte(val1.iVal, 4 + val2.iVal));
          } else
            return SimValue.errVal;
        }
      } else {
        // Must be a unary op
        if (si + 1 >= parts.length)
          return SimValue.errVal;
        val1 = getVal(tWidth, parts[si + 1]);
        if (parts[si].equals("-")) {
          if ((tWidth == 1) || (val1.width == 1))
            return new SimValue(1, -((byte) val1.cVal));
          else
            return new SimValue(4, -val1.iVal);
        } else if (parts[si].equals("+"))
          return val1;
        else if (parts[si].equals("!"))
          return new SimValue(4, (val1.iVal == 0) ? 0 : 1);
        else if (parts[si].equals("~")) {
          if ((tWidth == 1) || (val1.width == 1))
            return new SimValue(1, ~((byte) val1.cVal));
          else
            return new SimValue(4, ~val1.iVal);
        }
        else if (parts[si].equals("&")) {
          return new SimValue(4, getAddr(parts[si + 1]));
        } else if (parts[si].equals("#")) {
          return new SimValue(4, ptrGetInt(val1.iVal, 0));
        } else
          return SimValue.errVal;
      }
    }

    public boolean execLine() {
      debugPrint("Exec: " + lines[currLine]);
      String[] parts = lines[currLine].split(" +");
      if (parts[0].equalsIgnoreCase(".fnStart")) {
        int locSpace = Integer.parseInt(parts[1]);
        lSegment = new AddrSegment(locSpace);
      } else if (parts[0].equalsIgnoreCase(".fnEnd") || parts[0].equals("return")) {
        return false;
      } else if (parts[0].equals("return1")) {
        retVal = getVal(1, parts[1]);
        return false;
      } else if (parts[0].equals("return4")) {
        retVal = getVal(4, parts[1]);
        return false;
      } else if ((parts.length == 3) && (parts[1].contains("setsize"))) {
        setVal(parts[0], getVal(4, parts[2]));
      } else if (parts[0].equals("param1")) {
        SimValue pVal = getVal(1, parts[1]);
        debugPrint("Got val for param: " + pVal.cVal);
        argStack.push(pVal);
      } else if (parts[0].equals("param4")) {
        SimValue pVal = getVal(4, parts[1]);
        debugPrint("Got val for param: " + pVal.iVal);
        argStack.push(pVal);
      } else if ((parts[0].equals("call")) || ((parts.length > 2) && (parts[2].equals("call")))) {
        int si = 2;
        if (parts[0].equals("call"))
          si = 0;
        String[] callinfo = parts[si + 1].split(",");
        int numArgs = Integer.parseInt(callinfo[1]);
        List<SimValue> argList = new LinkedList<>();
        for (int i = 0; i < numArgs; i++) {
          if (argStack.empty())
            argList.add(SimValue.errVal);
          else
            argList.add(argStack.pop());
        }
        SimValue fnResult = executeFunction(callinfo[0], argList);
        if (si == 2)
          setVal(parts[0], fnResult);
      } else if (parts[0].equals("goto")) {
        Integer nextLine = labels.get(parts[1]);
        if (nextLine == null) {
          System.err.println("Jump to unknown label: " + parts[1]);
          return false;
        } else {
          currLine = nextLine;
          return true;
        }
      } else if (parts[0].equals("if")) {
        if (parts[2].equals("goto")) {
          int val = getVal(0, parts[1]).getAsI();
          if (val != 0) {
            Integer nextLine = labels.get(parts[3]);
            if (nextLine == null) {
              System.err.println("Jump to unknown label: " + parts[3]);
              return false;
            } else {
              currLine = nextLine;
              return true;
            }
          }
        } else {
          int result = calcVal(0, parts, 1).getAsI();
          if (result != 0) {
            Integer nextLine = labels.get(parts[5]);
            if (nextLine == null) {
              System.err.println("Jump to unknown label: " + parts[5]);
              return false;
            } else {
              currLine = nextLine;
              return true;
            }
          }
        }
      } else if (parts[0].equals("ifFalse")) {
        int val = getVal(0, parts[1]).getAsI();
        if (val == 0) {
          Integer nextLine = labels.get(parts[3]);
          if (nextLine == null) {
            System.err.println("Jump to unknown label: " + parts[3]);
            return false;
          } else {
            currLine = nextLine;
            return true;
          }
        }
      } else if ((parts.length > 1) && (parts[1].equals("="))) {
        if ((parts.length > 3) && parts[3].startsWith("stidx")) {
          // Special case for indexed array storage...
          int tWidth = parts[3].charAt(5) - '0';
          SimValue aRef = getVal(4, parts[0]);
          SimValue idx = getVal(4, parts[2]);
          SimValue rhs = getVal(tWidth, parts[4]);
          if (tWidth == 1)
            ptrSetByte(aRef.iVal, 4 + idx.iVal, rhs.cVal);
          else
            ptrSetInt(aRef.iVal, 4 + 4 * idx.iVal, rhs.iVal);
        } else {
          SimValue result = calcVal(getTWidth(parts[0]), parts, 2);
          debugPrint("Got value: " + result.getAsI());
          setVal(parts[0], result);
        }
      }

      currLine++;
      return true;
    }
  }

  public ICInterp(String code) {
    lines = code.split("\n");
    globals = new int[lines.length];
    segments = new Vector<>();
    segments.add(null); // Entry 0 is null (uninit/null ptr check)
    segFreeList = new Stack<>();
    dSegment = new AddrSegment(0);
    sin = new Scanner(System.in);

    labels = new HashMap<>();
    List<String> carryOverLabels = null;
    for (int i = 0; i < lines.length; i++) {
      String thisLabel = null;
      String[] parsed = lines[i].split(":");
      if (parsed[0].matches("[A-Za-z_][A-Za-z0-9_]*")) {
        thisLabel = parsed[0];
        labels.put(parsed[0], i);
        lines[i] = lines[i].substring(parsed[0].length() + 1);
      }
      lines[i] = lines[i].trim();
      if (lines[i].length() == 0) {
        if (thisLabel != null) {
          if (carryOverLabels == null)
            carryOverLabels = new LinkedList<>();
          carryOverLabels.add(thisLabel);
        }
      } else {
        if (thisLabel != null) {
          labels.put(thisLabel, i);
        }
        if (carryOverLabels != null) {
          for (String s : carryOverLabels)
            labels.put(s, i);
          carryOverLabels = null;
        }
      }

      if (lines[i].startsWith(".db ") || (lines[i].startsWith(".dw "))) {
        int elemSize = 1;
        if (lines[i].startsWith(".dw "))
          elemSize = 4;
        int valStart = 4;
        while ((valStart < lines[i].length()) && (lines[i].charAt(valStart) == ' '))
          valStart++;
        if (valStart == lines[i].length()) {
          globals[i] = dSegment.growBy(elemSize);
        } else {
          globals[i] = dSegment.getExtent();
          String[] vals = lines[i].substring(valStart).split(" *, *");
          for (String valInit : vals) {
            int nCopies = 1;
            int intVal = 0;
            if (valInit.contains("#")) {
              String[] parts = valInit.split("#");
              intVal = Integer.parseInt(parts[0]);
              nCopies = Integer.parseInt(parts[1]);
            } else {
              intVal = Integer.parseInt(valInit);
            }
            int ptr = dSegment.growBy(elemSize * nCopies);
            for (int j = 0; j < nCopies; j++) {
              if (elemSize == 1)
                ptrSetByte(ptr, j * elemSize, (byte) intVal);
              else
                ptrSetInt(ptr, j * elemSize, intVal);
            }
          }
        }
      }
    }
  }

  public SimValue executeFunction(String fname, List<SimValue> params) {
    if (fname.equals("gf_printd")) {
      if ((params != null) && (params.size() == 1))
        System.out.print(params.get(0).getAsI());
      return SimValue.errVal;
    } else if (fname.equals("gf_prints")) {
      if ((params != null) && (params.size() == 1)) {
        int ptr = params.get(0).getAsI();
        if (ptr == 0)
          return SimValue.errVal;
        int aLen = ptrGetInt(ptr, 0);
        for (int i = 0; i < aLen; i++) {
          char c = (char) ptrGetByte(ptr, 4 + i);
          if (c == '\0')
            break;
          System.out.print(c);
        }
      }
      return SimValue.errVal;
    } else if (fname.equals("gf_printc")) {
      if ((params != null) && (params.size() == 1))
        System.out.print((char) params.get(0).getAsC());
      return SimValue.errVal;
    } else if (fname.equals("gf_printf")) {
      if ((params != null) && (params.size() == 1))
        System.out.print((float) params.get(0).getAsF());
      return SimValue.errVal;
    } else if (fname.equals("gf_read")) {
      int readVal = sin.nextInt();
      return new SimValue(4, readVal);
    } else if (fname.equals("gf_readc")) {
      int readVal = sin.nextByte();
      return new SimValue(1, readVal);
    } else if (fname.equals("gf_readf")) {
      float readVal = sin.nextFloat();
      return new SimValue(4, readVal);
    } else if (fname.equals("gf_readline")) {
      if ((params != null) && (params.size() == 1)) {
        int ptr = params.get(0).getAsI();
        if (ptr == 0)
          return SimValue.errVal;
        int aLen = ptrGetInt(ptr, 0);

        String line = sin.nextLine();
        for (int i = 0; i < line.length() && i < aLen; i++) {
          ptrSetByte(ptr, 4 + i, (byte) line.charAt(i));
        }
        if (line.length() < aLen)
          ptrSetByte(ptr, 4 + line.length(), (byte) 0);
      }
      return SimValue.errVal;
    }
    debugPrint("Executing function " + fname + " (line " + labels.get(fname) + ")");
    Integer startLine = labels.get(fname);
    if (startLine == null) {
      System.err.println("Error in executing intermediate code: Unknown function " + fname);
      return SimValue.errVal;
    }
    LocalEnv env = new LocalEnv(startLine, params);

    while (env.execLine())
      ;

    SimValue retVal = env.getRetVal();
    env.freeSpace();

    return retVal;
  }
}