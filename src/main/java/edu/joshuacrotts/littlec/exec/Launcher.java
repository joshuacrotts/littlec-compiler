package edu.joshuacrotts.littlec.exec;

/**
 * Launcher for different functionality of the compiler.
 *
 * @author Steve Tate (srtate@uncg.edu)
 * 
 * @modified Joshua Crotts
 */
public class Launcher {
  
  public static void usage() {
    System.out.print("LittleC Compiler\n");
    System.out.print("Run as follows (input is from stdin unless a filename is given):\n\n");
    System.out.print("   littlec mode [optinfile]\n\n");
    System.out.print("where [optinfile] is a .lc file or standard input (leave blank)\n");
    System.out.print("where \"mode\" is one of the following:\n");
    System.out.print("   -lt runs LexerTest\n");
    System.out.print("   -pt runs ParserTest\n");
    System.out.print("   -st outputs the syntax tree (like pt without symbol tables)\n");
    System.out.print("   -pi produces intermediate code output with Quadruple structure.\n");
    System.out.print("   -pa produces MIPS assembly language output\n");
    System.out.print("   -rc runs code (from interpreting intermediate code)\n");
    System.out.print("   -rm runs MIPS assembly code (warning: you must have SPIM installed!)\n");

  }

  /**
   * Use the first command line argument to select the class that will be run.
   *
   * @param argv command line arguments
   */
  public static void main(String[] argv) {
    if ((argv.length < 1) || (argv.length > 2)) {
      usage();
      return;
    }

    String[] argsTail = new String[argv.length - 1];
    for (int i = 1; i < argv.length; i++)
      argsTail[i - 1] = argv[i];

    if (argv[0].equals("-lt"))
      LexerTest.main(argsTail);
    else if (argv[0].equals("-pt"))
      ParserTest.main(argsTail);
    else if (argv[0].equals("-pi"))
      OutputICode.main(argsTail);
    else if (argv[0].equals("-st"))
      OutputSTree.main(argsTail);
    else if (argv[0].equals("-rc"))
      RunCode.main(argsTail);
    else if (argv[0].equals("-pa"))
      OutputAsm.main(argsTail);
    else if (argv[0].equals("-rm")) {
      RunMIPS.main(argsTail);
    }
    else {
      usage();
    }
  }
}
