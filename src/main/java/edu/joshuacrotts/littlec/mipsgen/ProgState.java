package edu.joshuacrotts.littlec.mipsgen;

import java.util.*;

import edu.joshuacrotts.littlec.icode.ICAddress;

/**
 * Tracks the program state for a MIPS processor. Keeps track of where names
 * (Intermediate Code "Addresses") are stored (registers and memory) and tracks
 * what is stored in each register. Methods are provided to update the state
 * consistently, for each kind of basic operation (copying values, computing new
 * values, etc.)
 *
 * Each IC Address has a "canonical location" where it is considered stored by
 * default (until its location changes through method calls). The first time any
 * name is seen, it is assumed to be stored in its canonical location.
 * 
 * @author Steve Tate
 * 
 * @modified Joshua Crotts
 */
public class ProgState {
  private final Map<ICAddress, Boolean> addrHoldsName;
  private final Map<ICAddress, Set<MIPSReg>> addrDesc;
  private final Map<MIPSReg, Set<ICAddress>> regDesc;
  private final int paramsUsed;
  private final int localVarSize;
  private final MIPSFunction partOf;

  public ProgState(MIPSFunction partOf, int paramsUsed, int localVarSize) {
    addrHoldsName = new HashMap<>();
    addrDesc = new HashMap<>();
    regDesc = new HashMap<>();
    this.partOf = partOf;
    this.paramsUsed = paramsUsed;
    this.localVarSize = localVarSize;

    for (MIPSReg reg : MIPSReg.allReg()) {
      regDesc.put(reg, new HashSet<>());
    }
  }

  /**
   * If a name's canonical storage location is a register, return it. Otherwise,
   * return null.
   *
   * @param name the name to look up
   * @return canonical register or null if none
   */
  public MIPSReg getCanonicalReg(ICAddress name) {
    MIPSReg retVal = null;
    if (name.isParam()) {
      int pNum = name.getOffset() / 4;
      if (pNum < MIPSReg.NUM_AREG) {
        if (pNum < paramsUsed) {
          retVal = MIPSReg.sReg(pNum);
        } else {
          retVal = MIPSReg.aReg(pNum);
        }
      }
    }

    return retVal;
  }

  /**
   * Return the canonical location for an address. Some canonical locations (e.g.,
   * for local variables) depend on aspects of the stack frame size, which is set
   * up in the ProgState constructor. Canonical locations for temporary variables
   * are created on the fly, since most temporaries will never need a memory
   * location. To create a new temporary variable location, you call back to the
   * MIPSFunction object for the function that is being compiled, and that can
   * allocate a temporary variable location that is unique within the function.
   * This also allows the function code to know how much space is needed for
   * temporary variables, so that the epilogue and prologue can be created.
   * 
   * @param name the name
   * @return the canonical location for the name
   */
  public String getCanonicalMIPS(ICAddress name) {
    if (name.isLocal()) {
      return "" + (-localVarSize + name.getOffset()) + "($fp)";
    } else if (name.isParam()) {
      MIPSReg reg = getCanonicalReg(name);
      if (reg != null) {
        return reg.toString();
      } else {
        int offset = name.getOffset() - 4 * MIPSReg.NUM_AREG;
        return offset + "($s7)";
      }
    } else if (name.isTemp()) {
      return "-" + (localVarSize + partOf.getTVarLoc(name)) + "($fp)";
    } else {
      return name.toString();
    }
  }

  /**
   * We initialize names/addresses on the demand. This method is called at the top
   * of any method that uses a name, to make sure it exists. Any new name that is
   * seen is initialized to be stored only in its canonical location.
   * 
   * @param name the name ensure exists
   */
  private void initIfNeeded(ICAddress name) {
    if ((name != null) && !addrHoldsName.containsKey(name)) {
      addrHoldsName.put(name, true);
      addrDesc.put(name, new HashSet<>());
      MIPSReg reg = getCanonicalReg(name);
      if (reg != null)
        addrDesc.get(name).add(reg);
    }
  }

  /**
   * Gets a current register that has a copy of this name. Usually the first check
   * for where to access a named item, since accessing in a register is fastest.
   * If no register holds a copy of this name, then return null.
   * 
   * @param name the name to locate
   * @return a register holding the name
   */
  public MIPSReg getCurrReg(ICAddress name) {
    initIfNeeded(name);
    Set<MIPSReg> regs = addrDesc.get(name);
    if (regs.size() == 0)
      return null;
    else
      return regs.iterator().next();
  }

  /**
   * Gets an address where a name is stored. In the current implementation, a
   * named data item can only exist at its canonical location, and if that
   * location doesn't hold the current value then null is returned.
   * 
   * @param name the name to locate
   * @return its canonical location or null
   */
  public ICAddress getCurrAddr(ICAddress name) {
    initIfNeeded(name);
    if (addrHoldsName.get(name))
      return name;
    else
      return null;
  }

  /**
   * Returns a set of all ICAddresses being used for this register.
   * 
   * @param reg
   * @return
   */
  public Set<ICAddress> getICAddressAtReg(MIPSReg reg) {
    return this.regDesc.get(reg);
  }

  /**
   * Is the current value of the named item stored in its canonical location?
   * 
   * @param name the name to check
   * @return true if the canonical location holds an up-to-date value
   */
  public boolean isSaved(ICAddress name) {
    initIfNeeded(name);
    return addrHoldsName.get(name);
  }

  /**
   * Check to see if a register holds a named item.
   * 
   * @param reg  the register to check
   * @param addr the name to look for
   * @return true iff the reg has a current copy of the named item
   */
  public boolean regHolds(MIPSReg reg, ICAddress addr) {
    return regDesc.get(reg).contains(addr);
  }

  /**
   * Update state when a value is copied from a register to a name's canonical
   * location. Should be called after any kind of "store" instruction is
   * generated. Note that a register should only be copied to a canonical address
   * if the register holds a current copy of the named value. If this isn't the
   * case, a RuntimeException is thrown.
   *
   * @param name destination name
   * @param reg  source register
   */
  public void copyVal(ICAddress name, MIPSReg reg) {
    // No register contents change, and canonical addr marked as holding name.
    initIfNeeded(name);
    if (!regDesc.get(reg).contains(name)) {
      throw new RuntimeException("Tried to copy reg->name, but reg doesn't hold the name");
    }
    addrHoldsName.put(name, true);
  }

  /**
   * Update state when a value is copied from an address to a register, where the
   * address is given by the location's canonical name. Should be called after any
   * kind of "load" instruction is generated. Note that if you try to copy from a
   * name's canonical address, and that address doesn't hold a current copy of the
   * name, then a RuntimeException is thrown. This should never happen!
   *
   * @param reg  destination register
   * @param name source - use null if no source address (i.e., constant)
   */
  public void copyVal(MIPSReg reg, ICAddress name) {
    initIfNeeded(name);

    if ((name != null) && !addrHoldsName.get(name)) {
      throw new RuntimeException("Tried to copy name->reg, but address doesn't hold the name");
    }

    // Register loses it's prior contents and names lose that storage loc
    for (ICAddress a : regDesc.get(reg)) {
      addrDesc.get(a).remove(reg);
    }
    regDesc.get(reg).clear();

    // Set up new value
    if (name != null) {
      addrDesc.get(name).add(reg);
      regDesc.get(reg).add(name);
    }
  }

  /**
   * Update state when a value is copied from one register to another. Reflects a
   * MIPS "move" instruction
   * 
   * @param dreg destination
   * @param sreg source
   */
  public void copyVal(MIPSReg dreg, MIPSReg sreg) {
    // Dest register loses it's prior contents and names lose that storage loc
    for (ICAddress a : regDesc.get(dreg)) {
      addrDesc.get(a).remove(dreg);
    }
    regDesc.get(dreg).clear();

    // Set up new value - duplicates names held by source register
    for (ICAddress a : regDesc.get(sreg)) {
      addrDesc.get(a).add(dreg);
      regDesc.get(dreg).add(a);
    }
  }

  /**
   * Set this register as the unique location for a new (replacement) value for
   * the name. Invalidates all other storage locations for that name since they
   * refer to the previous value. Used after a computation instruction creates a
   * new value for the name.
   *
   * @param reg  the new, unique, register location for the name
   * @param name the name
   */
  public void createValueInRegister(MIPSReg reg, ICAddress name) {
    initIfNeeded(name);
    // Register loses it's prior contents and names lose that storage loc
    for (ICAddress a : regDesc.get(reg)) {
      addrDesc.get(a).remove(reg);
    }
    regDesc.get(reg).clear();

    // All previous copies need to be invalidated (including at canonical addr)
    for (MIPSReg r : addrDesc.get(name)) {
      regDesc.get(r).remove(name);
    }
    addrDesc.get(name).clear();
    addrHoldsName.put(name, false);

    // Set up new value
    addrDesc.get(name).add(reg);
    regDesc.get(reg).add(name);
  }

  /**
   * Set this register as a location for the name. If reg wasn't already a
   * location, then it is assumed to be a new value and all previous locations
   * associated with the name are invalidated.
   *
   * @param reg  the new, unique, register location for the name
   * @param name the name
   */
  public void setValueInRegister(MIPSReg reg, ICAddress name) {
    initIfNeeded(name);
    if (!regDesc.get(reg).contains(name)) {
      // Invalidate previous locations
      for (MIPSReg r : addrDesc.get(name)) {
        regDesc.get(r).remove(name);
      }
      addrDesc.get(name).clear();
      addrHoldsName.put(name, false);

      // Assign name to reg
      addrDesc.get(name).add(reg);
      regDesc.get(reg).add(name);
    }
  }

  /**
   * Invalidate a register - removes it as a storage location for all the names
   * currently there. Doesn't make sure there's another copy elsewhere, so be
   * careful not to lose your values!
   *
   * @param reg the register that is no longer being used
   */
  public void invalidate(MIPSReg reg) {
    for (ICAddress a : regDesc.get(reg)) {
      addrDesc.get(a).remove(reg);
    }
    regDesc.get(reg).clear();
  }

  /**
   * Get a list of names whose only storage location is reg. Call just before
   * changing reg so that you can store any of these values back to the canonical
   * address.
   *
   * @param reg the register you are querying
   * @return names that are only stored in reg
   */
  public List<ICAddress> uniquelyHere(MIPSReg reg) {
    List<ICAddress> retList = new LinkedList<>();
    for (ICAddress a : regDesc.get(reg)) {
      if (!addrHoldsName.get(a) && (addrDesc.get(a).size() == 1))
        retList.add(a);
    }

    return retList;
  }

  /**
   * Get an available register, avoiding two specific registers (may be null if no
   * restrictions). Get a free register if possible, and if not just pick the
   * first non-avoiding register.
   * 
   * @param avoid1
   * @param avoid2
   * @return
   */
  public MIPSReg getNextAvailableRegister(MIPSReg avoid1, MIPSReg avoid2) {
    for (MIPSReg tr : MIPSReg.allTReg()) {
      if ((regDesc.get(tr).size() == 0) && (tr != avoid1) && (tr != avoid2)) {
        return tr;
      }
    }

    if ((avoid1 != MIPSReg.tReg(0)) && (avoid2 != MIPSReg.tReg(0))) {
      return MIPSReg.tReg(0);
    }
    if ((avoid1 != MIPSReg.tReg(1)) && (avoid2 != MIPSReg.tReg(1))) {
      return MIPSReg.tReg(1);
    }
    return MIPSReg.tReg(2);
  }

  /**
   * Get an available register, with no preference to what it is.
   * 
   * @return next available temporary register.
   */
  public MIPSReg getNextAvailableRegister() {
    return getNextAvailableRegister(null, null);
  }

  /**
   * Invalidates all temporary registers.
   * 
   * @param void.
   * 
   * @return void.
   */
  public void clearTempRegisters() {
    for (int i = 0; i < MIPSReg.NUM_TREG; i++) {
      this.invalidate(MIPSReg.tReg(i));
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Reg:[");
    boolean firstReg = true;
    for (int i = 0; i < 10; i++) {
      MIPSReg reg = MIPSReg.tReg(i);
      Set<ICAddress> inReg = regDesc.get(reg);
      if (!inReg.isEmpty()) {
        if (!firstReg)
          sb.append(" ");
        firstReg = false;
        sb.append(reg);
        sb.append("={");
        boolean firstIn = true;
        for (ICAddress a : inReg) {
          if (!firstIn)
            sb.append(",");
          sb.append(a);
          firstIn = false;
        }
        sb.append("}");
      }
    }
    sb.append("]  Addr:[");
    boolean firstaddr = true;
    for (ICAddress a : addrDesc.keySet()) {
      if (!firstaddr)
        sb.append(' ');
      firstaddr = false;
      sb.append(a);
      sb.append("={");
      boolean firstloc = true;
      if (addrHoldsName.get(a)) {
        sb.append("mem");
        firstloc = false;
      }
      for (MIPSReg r : addrDesc.get(a)) {
        if (!firstloc)
          sb.append(",");
        firstloc = false;
        sb.append(r);
      }
      sb.append("}");
    }
    sb.append("]");
    return sb.toString();
  }
}
