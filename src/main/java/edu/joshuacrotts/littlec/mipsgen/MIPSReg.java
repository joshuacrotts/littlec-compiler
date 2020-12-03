package edu.joshuacrotts.littlec.mipsgen;

import java.util.ArrayList;
import java.util.List;

/**
 * MIPS register model. Since there is a fixed set of registers, using a static
 * allocation of objects and a private constructor to make sure there's only one
 * instance of each register object. That way they can be compared just by
 * comparing references.
 * 
 * @author Steve Tate
 */
public class MIPSReg {
  // These are public, so you can access these from outside the class
  public static final int NUM_AREG = 4;
  public static final int NUM_SREG = 8;
  public static final int NUM_TREG = 10;

  // These are the statically-allocated register objects
  private static MIPSReg[] regObj;

  // Set up for easy iteration through all temporary registers
  private static List<MIPSReg> tRegList;
  private static List<MIPSReg> allRegList;

  private final int regNum;
  private final String name;

  // Set up all the registers
  static {
    if (regObj == null) {
      regObj = new MIPSReg[32];
      regObj[0] = new MIPSReg(0, "$zero");
      regObj[1] = new MIPSReg(1, "$at");
      regObj[2] = new MIPSReg(2, "$v0");
      regObj[3] = new MIPSReg(3, "$v1");
      regObj[4] = new MIPSReg(4, "$a0");
      regObj[5] = new MIPSReg(5, "$a1");
      regObj[6] = new MIPSReg(6, "$a2");
      regObj[7] = new MIPSReg(7, "$a3");
      regObj[8] = new MIPSReg(8, "$t0");
      regObj[9] = new MIPSReg(9, "$t1");
      regObj[10] = new MIPSReg(10, "$t2");
      regObj[11] = new MIPSReg(11, "$t3");
      regObj[12] = new MIPSReg(12, "$t4");
      regObj[13] = new MIPSReg(13, "$t5");
      regObj[14] = new MIPSReg(14, "$t6");
      regObj[15] = new MIPSReg(15, "$t7");
      regObj[16] = new MIPSReg(16, "$s0");
      regObj[17] = new MIPSReg(17, "$s1");
      regObj[18] = new MIPSReg(18, "$s2");
      regObj[19] = new MIPSReg(19, "$s3");
      regObj[20] = new MIPSReg(20, "$s4");
      regObj[21] = new MIPSReg(21, "$s5");
      regObj[22] = new MIPSReg(22, "$s6");
      regObj[23] = new MIPSReg(23, "$s7");
      regObj[24] = new MIPSReg(24, "$t8");
      regObj[25] = new MIPSReg(25, "$t9");
      regObj[26] = new MIPSReg(26, "$k0");
      regObj[27] = new MIPSReg(27, "$k1");
      regObj[28] = new MIPSReg(28, "$gp");
      regObj[29] = new MIPSReg(29, "$sp");
      regObj[30] = new MIPSReg(30, "$fp");
      regObj[31] = new MIPSReg(31, "$ra");

      tRegList = new ArrayList<>();
      for (int i = 0; i < NUM_TREG; i++) {
        tRegList.add(tReg(i));
      }

      allRegList = new ArrayList<>();
      for (MIPSReg reg : regObj)
        allRegList.add(reg);
    }
  }

  /**
   * The constructor - notice that it is private because register objects should
   * not be created anywhere other than the static initializer above.
   *
   * @param regNum the register number
   * @param name   the printable name
   */
  private MIPSReg(int regNum, String name) {
    this.regNum = regNum;
    this.name = name;
  }

  /**
   * Get the register object for a temporary register, by number. For example,
   * MIPSReg.tReg(4) gives the register object for $t4.
   * 
   * @param num the temporary number
   * @return the register object
   */
  public static MIPSReg tReg(int num) {
    if (num < 8)
      return regObj[8 + num];
    else if (num < 10)
      return regObj[16 + num];
    else
      return null;
  }

  /**
   * Get the register object for an argument register, by number. For example,
   * MIPSReg.aReg(3) gives the register object for $a3.
   * 
   * @param num the argument register number
   * @return the register object
   */
  public static MIPSReg aReg(int num) {
    if (num < 4)
      return regObj[4 + num];
    else
      return null;
  }

  /**
   * Get the register object for a saved register, by number. For example,
   * MIPSReg.sReg(3) gives the register object for $s3.
   * 
   * @param num the saved register number
   * @return the register object
   */
  public static MIPSReg sReg(int num) {
    if (num < 8)
      return regObj[16 + num];
    else
      return null;
  }

  /**
   * Gets the register object for a return register.
   * 
   * @param num
   * @return
   */
  public static MIPSReg vReg(int num) {
    if (num >= 0 && num <= 1)
      return regObj[2 + num];
    else
      return null;
  }

  /**
   * Gives an iterable list of temporary registers. Useful for flushing all
   * temporary variables when the end of a basic block is reached.
   * 
   * @return the list for iterating through
   */
  public static List<MIPSReg> allTReg() {
    return tRegList;
  }

  /**
   * Gives an iterable list of all registers.
   * 
   * @return the list for iterating through
   */
  public static List<MIPSReg> allReg() {
    return allRegList;
  }

  /**
   * Get the printable name for this register.
   * 
   * @return the name
   */
  public String getName() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    MIPSReg r = (MIPSReg) o;

    return this.toString().equals(r.toString());
  }

  @Override
  public String toString() {
    return name;
  }

  // Just need a place to put this.....
  private static final String fileHeader = "        .text\n" + "\n" + "gf_printd:\n\tli $v0, 1\n" + "\tsyscall\n"
      + "\tjr $ra\n" + "\n" + "gf_printc:\n" + "\tli $v0, 11\n" + "\tsyscall\n" + "\tjr $ra" + "\n\n" + "gf_printf:\n"
      + "\tli $v0, 2\n" + "\tsyscall\n" + "\tjr $ra" + "\n\n" + "gf_prints:\n\tld $t1, 0($a0)\n" + "\taddi $t0, $a0, 4\n"
      + "\n" + "z1:\tlb $a0, 0($t0)\n" + "\tbeqz $a0, z2\n" + "\n" + "\tli $v0, 11\n" + "\tsyscall\n" + "\t\n"
      + "\taddi $t0, $t0, 1\n" + "\taddi $t1, $t1, -1\n" + "\tbgtz $t1, z1\n" + "\t\n" + "z2:\tjr $ra\n" + "\n"
      + "gf_readline:\n" + "\tlw $a1, 0($a0)\n" + "\tadd $a0, $a0, 4\n" + "\tli $v0, 8\n" + "\tsyscall\n" + "\tjr $ra\n"
      + "\n" + "gf_read: \n" + "\tli $v0, 5\n" + "\tsyscall\n" + "\tjr $ra\n" + "\n" + "gf_readc:\n" + "\tli $v0, 12\n"
      + "\tsyscall\n" + "\tjr $ra" + "\n\n" + "main:   j gf_main\n" + "\n";

  public static String stdFunctions() {
    return fileHeader;
  }
}
