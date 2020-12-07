package edu.joshuacrotts.littlec.mipsgen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * This class serves as secondary traversals to code generation optimization, in
 * that we generate MIPS code, then we traverse through this code to find any
 * optimizations.
 * 
 * @author Joshua
 *
 */
public class MIPSOptimizer {

  /**
   * Optimizes a MIPS string representation.
   * 
   * @param mipsString - toString representation of MIPS code.
   * 
   * @return optimized MIPS string.
   */
  public static String optimize(String mipsString) {
    // First, pass through and remove redundant loads.
    String formerMIPSString = new String(mipsString);

    // Do as many passes as it takes before we get to a point when we can
    // say that the previous traversal removed no code.
    do {
      // First, remove redundant load operations.
      String[] splitArr = mipsString.split("\n");
      ArrayList<String> arr = new ArrayList<String>(Arrays.asList(splitArr));
      mipsString = removeRedundantLoads(arr);

      // Then, go back and remove all unnecessary loads.
      splitArr = mipsString.split("\n");
      arr = new ArrayList<String>(Arrays.asList(splitArr));
      mipsString = removeUnnecessaryLoads(arr);

      // Now remove the duplicate labels.
      splitArr = mipsString.split("\n");
      arr = new ArrayList<String>(Arrays.asList(splitArr));
      mipsString = removeDuplicateLabels(arr);

      // Finally, reassign the string.
      formerMIPSString = new String(mipsString);
    } while (!mipsString.equals(formerMIPSString));

    return mipsString;
  }

  /**
   * Optimizes stores and loads (or loads and loads) from the same location to a
   * store then a move.
   * 
   * For instance,
   * 
   * sw $t3, -88($fp)
   * lw $a0, -88($fp)
   * 
   * can be optimized to
   * 
   * la $t3, -60($fp) 
   * move $a0, $t3
   * 
   * @param ArrayList<String> of MIPS code, delimited by newlines.
   * 
   * @return string with all unnecessary loads removed.
   */
  private static String removeUnnecessaryLoads(ArrayList<String> mipsSplitList) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < mipsSplitList.size() - 1; i++) {
      String inst1 = mipsSplitList.get(i);
      String inst2 = mipsSplitList.get(i + 1);

      // If our first character is a store, then a load, OR
      // it's a store then a load...
      if ((inst1.matches("^\ts[b|w](.*)") && inst2.matches("^\tl[b|a|i|w](.*)"))
          || (inst1.matches("^\tl[b|a|i|w](.*)") && inst2.matches("^\ts[b|w](.*)"))) {

        String inst1Loc = inst1.substring(inst1.lastIndexOf(", "));
        String inst2Loc = inst2.substring(inst2.lastIndexOf(", "));

        if (inst1Loc.equals(inst2Loc)) {
          String reg1 = inst1.substring(inst1.indexOf("$"), inst1.indexOf(","));
          String reg2 = inst2.substring(inst2.indexOf("$"), inst2.indexOf(","));
          mipsSplitList.set(i + 1, "\tmove " + reg2 + ", " + reg1);
          i = 0;
        }
      }
    }

    // Second traversal to build str.
    for (int i = 0; i < mipsSplitList.size(); i++) {
      sb.append(mipsSplitList.get(i));
      sb.append("\n");
    }

    return sb.toString();
  }

  /**
   * Removes redundant load statements. For instance, if we store a value from a
   * register into memory, then load that value immediately back out to the same
   * register, we can just get rid of that.
   * 
   * Example:
   * sw $t0, -4($fp)
   * lw $t0, -4($fp)
   * 
   * is optimized to
   * 
   * sw $t0, -4($fp)
   * 
   * @param ArrayList<String> of MIPS code, delimited by newlines.
   * 
   * @return string with all redundant loads removed.
   */
  private static String removeRedundantLoads(ArrayList<String> mipsSplitList) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < mipsSplitList.size() - 1; i++) {
      String inst1 = mipsSplitList.get(i);
      String inst2 = mipsSplitList.get(i + 1);

      if ((inst1.matches("^\ts[b|w](.*)") && inst2.matches("^\tl[b|a|i|w](.*)"))) {

        inst1 = inst1.substring(3);
        inst2 = inst2.substring(3);

        if (inst1.equals(inst2)) {
          mipsSplitList.remove(i + 1);
          i = 0;
        }
      }
    }

    // Second traversal to build str.
    for (int i = 0; i < mipsSplitList.size(); i++) {
      sb.append(mipsSplitList.get(i));
      sb.append("\n");
    }

    return sb.toString();
  }

  /**
   * Due to how my IC generation works, duplicate labels are a possibility with
   * complex SC evaluation. This method traverses the code generation backwards,
   * and removes duplicate labels once they've been seen.
   * 
   * @param ArrayList<String> of MIPS code, delimited by newlines.
   * 
   * @return string with all duplicate (erroneous) labels removed.
   */
  private static String removeDuplicateLabels(ArrayList<String> mipsSplitList) {
    StringBuilder sb = new StringBuilder();
    Set<String> visitedLabels = new HashSet<>();

    for (int i = mipsSplitList.size() - 1; i >= 0; i--) {
      String inst1 = mipsSplitList.get(i);

      if (inst1.matches("^L[\\d]+:")) {
        if (visitedLabels.contains(inst1)) {
          mipsSplitList.remove(i);
          continue;
        }

        visitedLabels.add(inst1);
      }
    }

    // Second traversal to build str.
    for (int i = 0; i < mipsSplitList.size(); i++) {
      sb.append(mipsSplitList.get(i));
      sb.append("\n");
    }

    return sb.toString();

  }
}
