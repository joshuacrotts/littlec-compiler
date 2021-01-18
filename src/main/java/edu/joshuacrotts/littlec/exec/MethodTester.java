package edu.joshuacrotts.littlec.exec;

import java.util.ArrayList;

public class MethodTester {

  public static void main(String[] args) {
    ArrayList<Integer> nums = new ArrayList<>();
    int m= 5;
    int n = 100;
    for (int i = m; i <=n; i++) {
      nums.add(i);
    }
    
    while (nums.size() != 1) {
      int idx = (int) (Math.random() * nums.size());
      System.out.print(nums.remove(idx));
    }
    
    System.out.println("\nMissing: " + nums.get(0));
  }
}
