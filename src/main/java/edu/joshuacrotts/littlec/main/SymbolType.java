package edu.joshuacrotts.littlec.main;

public enum SymbolType {

  VAR("VAR"),
  FNDEF("FNDEF"),
  FNPROTOTYPE("FNPROTOTYPE");
  
  private String strRep;
  
  private SymbolType(String str) {
    this.strRep = str;
  }
  
  @Override
  public String toString() {
    return this.strRep;
  }
}
