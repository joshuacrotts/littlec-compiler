
## Assignment 5 -- Code Generation -- Due Mon, November 30 ##

This is the final stage of the compiler project! In this stage, you
should write code to turn intermediate code into MIPS assembly
language. It should be possible to use your MIPS assembly output with
[SPIM](http://spimsimulator.sourceforge.net/), the MIPS simulator, to
run a compiled program. SPIM runs on all major platforms, including
Windows, Linux, and Mac OS-X, and you can download a copy from the web
site. You can also use the command-line version `spim` on UNCG's Linux
host. The `littlec` program on the UNCG Linux system will soon be able
to output MIPS assembly, so you can experiment to see what code my
solution outputs.

### MIPS Assembly Language

The SPIM web page has a page for "[Further
Information](http://spimsimulator.sourceforge.net/further.html)" which
includes a link to a copy of "[Appendix
A](http://spimsimulator.sourceforge.net/HP_AppA.pdf)" from the
Patterson and Hennessy Computer Organization textbook, which has full
documentation for the MIPS architecture and SPIM. The most important
parts of this document are Sections A.6, A.9, and A.10. Information
about exceptions, traps, floating point or coprocessor instructions,
and I/O are all irrelevant for the CSC 439 project. In addition, you
will not be handling system calls in your code but rather will be
calling functions for the provided standard functions (`prints`,
`printd`, etc.).

You may take a simple approach to register allocation, dealing with
allocation one basic block at a time. Note that in addition to the
basic block rules described in the textbook (and in the class
lecture), you should consider a `call` instruction similar to a jump,
so the line following a `call` is a leader that starts a new block.
Since you are allocating registers only within basic blocks, and basic
blocks cannot extend past a `call`, no temporary register values will
be preserved across a function call and all temporaries can use `$t`
registers. You *should* use `$s` registers to save function arguments
(entering the function in `$a` registers) if those registers are
re-used for function arguments. See the "calling conventions" section
below for more information on this.

Note that some operations, such as addition and subtraction, have
versions "with overflow" and "without overflow." The C language does
not detect overflow on integer operations, so LittleC should not
either, and you should always use the "without overflow" versions of
these operations (i.e., use `addu` and `subu`).

### Assignment Grading Guidelines

You can get up to 80 points by just supporting the `int` type and the
ability to pass a string literal to `prints`. At the 80 point level
you must still be able to handle all statement types and operators,
including function calls and return values, but you don't need to
support the `char` type or arrays. You must follow the MIPS calling
conventions, including handling caller and callee-saved registers, but
no optimizations or register minimization is required at this level.

To get above 80 points, you must support additional features of
LittleC. You can support any subset of the following features, so if
you support all 4 you can get a 100 on the assignment.

* 5 points for supporting the `char` type as well (see the language note below).

* 5 points for ordering subexpression evaluation (like in Ershov
  numbering) to minimize register use.

* 5 points for supporting `char` arrays. 

* 5 points for supporting `int` arrays. 

* *Extra credit:* Any additional optimizations that you include will
  give you extra credit, up to 25 points maximum. The amount for each
  optimization depends on the difficulty. For example, optimizing code
  for loading parameters into the `$a` registers (see below) is worth
  an extra 5 points. Doing optimization that requires analysis across
  multiple basic blocks is worth an extra 10 points. If you do any
  "extras" like this, email me and let me know what you did so that I
  can check it.

### LittleC Language Notes 

The change from the originally-planned x86 target architecture to
MIPS, as well as the unexpected difficulty of local array
initialization, leads to the following two changes for this stage of
the assignment.

* The MIPS processor is like most RISC processors in that operations
  are performed only using registers or immediate (constant)
  values. Since all registers are 32-bit registers, we will relax the
  original language requirement that used byte operations for
  calculations with `char` types, and say that all arithmetic is
  performed on 32-bit values, giving a 32-bit result. That means that
  the only time you have to consider the type of an integer value is
  when it is loaded into a register from memory (using the `lb`
  assembly language instruction to load a `char`) or stored to memory
  (using the `sb` instruction). At all other times these values are
  treated as 32-bit integers.

* While the original language specification said that any character
  array could be initialized using a string literal, this is entirely
  optional for character arrays that are local variables. This is a
  little trickier than I originally thought, so it does not need to be
  done.

### Code Generation

It is simplest, and perfectly acceptable for this assignment, to break
the code up into basic blocks and generate code for each basic block
independently of the rest of the code. That means that, with one
exception, register allocations are reset at the beginning of each
basic block. Therefore, any memory-based variable (local, global, or
temporary) required in a basic block must be loaded into a register in
that block, and if it is modified in a register then that register
must be stored back to memory at the end of the basic block.

The one exception to the block-local behavior of registers is for the
first four parameter values to a function. The first 4 parameters to
any function are passed in the `$a0` through `$a3` registers, and will
stay in registers throughout the execution of the function. They might
not be in the `$a` registers, however! As shown in the `tak` function
example on pages A-30 to A-33 of the MIPS/SPIM document, since `$a`
registers may be needed to pass arguments to other functions, you must
first save any such registers in `$s` registers if other function
calls are made. See below for more information and an example. To
determine how many `$a` registers are used in calling other functions,
you can first just scan through all the lines in the function. Since
every `call` line will say how many parameters are being passed, you
just need to get the maximum of all these values.

### Provided Classes

To make this assignment a little easier, I have provided two classes
in the assignment "starting code". These are documented in the code,
but here's a quick description:

* `MIPSReg` is a class that simplifies working with MIPS
  registers. This class will produced printable names for any
  register, allows you to iterate through the temporary registers, and
  more.

* `ProgState` is a class that tracks where each named data item is
  stored (the "address descriptor") and what is stored in each
  register (the "register descriptor"). Methods are provided that you
  can call when data is copied or when new values are computed. You
  can query this to find out if a name is currently stored in a
  register, whether it's stored in memory, and more.

You are not required to use these classes, and in order to use them
with your intermediate code classes you will need to modify from my
use of the `ICAddress` class (my class for tracking "addresses" or
names in 3-address code). Regardless of whether you use these classes
or not, it's probably worth your time to look through them and
understand how they work.

### Canonical Locations

Each name from the intermediate-code has a "canonical location" where
it is stored when not in use. Canonical locations are expressed using
MIPS addressing modes, so that values can be loaded from or stored to
canonical locations easily. For example, if there is one local
variable, taking up 4 bytes, then the canonical location for name
`l4@0` is `-4($fp)`. The canonical location for `p4@0` is register
`$a0`. The provided `ProgState` class has a method that can take any
intermediate-code name ant produce its canonical address, so you can
use that in a MIPS instruction.

### Calling conventions, function prologue and epilogue

Calling conventions are described in Section A.6 of the MIPS
reference. We supplement this in one way, by assigning a fixed use for
callee-saved register `$s7`, which we describe below.

Each function should start with a "prologue" that initializes
registers and the stack frame for executing this function, and ends in
an "epilogue" that resets the state and returns to the calling
function. To create the prologue and epilogue, you need to know which
"callee-saved" registers (the `$s` registers) are used, how much space
is needed for local and memory-saved temporary variables, and how many
parameters are used by functions called from this function (so that
the appropriate `$a` registers can be transferred to saved registers).

From highest memory location to lowest, the following is saved in the
stack frame: any parameters after the 4th parameter, then the return
address, the saved frame pointer, the saved `$s7` register, any other
callee-saved registers (`$s` registers that are used in this
function), local variables, and then memory-saved temporary
variables. When starting to generate code for a function you can (and
should!) do an initial scan through the function's lines to determine
the largest number of parameters passed to any function called from
here, which tells you which `$a` registers (if any) need to be
transferred to `$s` registers. However, other parts of the prologue,
such as the number of temporary variables required, cannot be
determined until *after* the code is generated. That means you will
either have to back-patch values in the prologue after the code is
generated, or you can actually generate the whole prologue after the
code generation and insert it at the beginning of the function. After
the epilogue is executed, the frame pointer `$fp` should be pointing
just past the local variables.

Note that a variable number of callee-saved variables are saved
starting at the frame's `$fp` location, and then the fifth and
subsequent parameters (if any) sit on top of the return
address. However, since the number of callee-saved registers used by a
function is not necessarily known until after the code for the
function is generated, the address of these parameters relative to
`$fp` cannot be determined until after code for the function is
generated. To avoid having to backpatch, we use `$s7` as a secondary
frame pointer, where it points to the location of the fifth parameter
(if it exists). Since this is a callee-saved register, we must save it
in the prologue and restore it in the epilogue as well. The following
picture shows the layout of the stack frame after it is set up by the
prologue.

![](stackframe.png)

Consider a global function named `ex` (so has mangled name `gf_ex`)
that takes 3 parameters, and calls another function that takes two
parameters. Our function requires 12 bytes for local variables, and 4
bytes for one memory-saved temporary variable. Therefore, the stack
frame must have three words (12 bytes) to save the standard
callee-saved registers (the return address, old frame pointer, and old
`$s7`), two words (8 bytes) for callee-saved registers for the two
re-used `$a` registers, and the 16 bytes for local and temporary
variables, bringing the total size of the stack frame to 12+4+20=36
bytes). As mentioned in the documentation, MIPS requires the stack
pointer to be aligned on 8-byte boundaries, so the space allocated to
the stack frame must be a multiple of 8 and we have to round the stack
frame size up to 40 bytes in this case. Given all of this information,
here's what the function prologue looks like for this function:

```
gf_ex:
        .globl gf_ex
        subu $sp, $sp, 40
        sw $ra, 32($sp)
        sw $fp, 28($sp)
        sw $s7, 24($sp)
        sw $s0, 16($sp)
        move $s0, $a0
        sw $s1, 20($sp)
        move $s1, $a1
        addiu $fp, $sp, 16
        addiu $s7, $sp, 40
```
The function epilogue looks like this:
```
xf_ex:
        lw $s0, 16($sp)
        lw $s1, 20($sp)
        lw $s7, 24($sp)
        lw $fp, 28($sp)
        lw $ra, 32($sp)
        addiu $sp, $sp, 40
        jr $ra
```

Note the label `xf_ex` on the epilogue -- this is a label created during
code generation. Any return statement in the LittleC program must
perform the epilogue "clean-up", and so should jump to this label to
actually perform the return.

### More on Calling Function and Parameters

Function calls and parameters are little bit trickier in MIPS than in
the originally-planned Intel target. In particular, since some
parameters get passed in registers rather than on the stack, not all
`param` intermediate-code statements are the same. Furthermore,
because of C's convention of pushing parameters right-to-left, and
`$a0` to `$a3` being the leftmost parameters, you don't even know
whether a parameter should go into a register (and which register) or
get pushed onto the stack when you see the `param` statement. One
solution is to simply push everything on the stack, as if all
parameters were passed on the stack, and then pop into the `$a`
registers immediately before the call. This works, and is not
difficult to code up, but is not efficient. For example, a call like
`f(1)` would generate this code:

```
        li $t0, 1
        subu $sp, $sp, 4
        sw $t0, 0($sp)
        lw $a0, 0($sp)
        addu $sp, $sp, 4
        jal gf_f
```
when all you *really* need is:
```
        li $a0, 1
        jal gf_f
```
A better solution, but harder to code is this: create new
temporary variables for the parameters, and your normal temporary
variable handling code can spill those to memory if needed. Then
immediately before the call you can copy those temporary variables
into the `$a` registers. This requires some unnecessary
register-to-register copies, but that's a lot faster than unnecessary
memory accesses. Better solutions (computing directly into the `$a`
registers requires more sophisticated code analysis -- see the extra
credit possibilities!

### Supporting Standard Functions

The four standard functions, `printd`, `prints`, `readline`, and
`read`, have all been implemented in MIPS assembly language, and your
code can call these just like any other function. However, you need to
include the code in your assembly output. In my provided code is a
static method `MIPSReg.stdFunctions()` that returns a string
containing these functions. Simply get this string and output it at
the top of your compiler output, and then you should be able to use
these functions just like any other function.

### Tips and Hints

* Do not use the `.align` directive. While it looks tempting, since
  data should be aligned, if you never explicitly give the alignment
  directive then data will be automatically aligned properly.

* Develop this in clear phases. For example, start off with a
  `getReg()` function that doesn't even try to do intelligent register
  allocation, and don't rearrange instructions. Just always use `$t0`
  for the destination and `$t1` and `t2` for the operands (assuming
  there are two operands). This is terribly inefficient, since
  registers are regularly reused for new purposes and so there are
  lots of unnecessary loads and stores, but it is a good test to make
  sure you are correctly updating the ProgState object to track what
  is stored in variables and that code generation works properly. Only
  after you get that working, go back and think about more intelligent
  algorithms for `getReg()`.

* While the previous tip said to develop in phases, plan ahead for
  future phases. In other words, while that initial verion os
  `getReg()` doesn't do anything interesting, you should still call it
  as if it were the final version (don't hard code those register
  values!). And while you aren't rearranging instructions at first,
  have a function `reOrder` that can reorder your code and call that
  before generating code - the first version doesn't do anything at
  all (just uses the original order of the intermediate code
  instructions), but call it anyway so you can just drop in a
  replacement for that function later and everything else should still
  work.

* Be careful with the order of code at the very end of a block. In
  particular, any values stored in registers that will be needed later
  should be saved to memory, but you can't just flush values after
  generating all of the block's code. In particular, if the last
  instruction in the block is a control-flow transfer instruction, you
  need to make sure your register saves are *before* that last
  instruction!


* Label placement in the MIPS code can be tricky if you don't realize
  one very simple fact: Each basic block can have only one label, and
  it can only be on the first line in the block. Therefore, if you
  look at the first line of intermediate code to see if has a label,
  you can output a label-only line before outputting any of the code
  for the block.

&nbsp;



