package edu.joshuacrotts.littlec.main;

public enum StorageClass {

  EXTERN("extern"),
  STATIC("static"),
  DEFAULT("");
  
  private String strRep;
  
  private StorageClass(String str) {
    this.strRep = str;
  }
  
  @Override
  public String toString() {
    return this.strRep;
  }
     
}
