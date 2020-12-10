<h1 align="center">LittleC Compiler</h1>

[![CodeFactor](https://www.codefactor.io/repository/github/joshuacrotts/LittleC-Compiler/badge)](https://www.codefactor.io/repository/github/joshuacrotts/LittleC-Compiler) ![](https://github.com/JoshuaCrotts/LittleC-Compiler/workflows/tests/badge.svg) ![GitHub contributors](https://img.shields.io/github/contributors/JoshuaCrotts/LittleC-Compiler) ![GitHub commit activity](https://img.shields.io/github/commit-activity/m/JoshuaCrotts/LittleC-Compiler) ![GitHub repo size](https://img.shields.io/github/repo-size/JoshuaCrotts/LittleC-Compiler) [![GitHub issues open](https://img.shields.io/github/issues/JoshuaCrotts/LittleC-Compiler)]() 
[![GitHub issues closed](https://img.shields.io/github/issues-closed-raw/JoshuaCrotts/LittleC-Compiler)]()

LittleC is a small, C-like language written for my compilers course. Some segments of the code, including the interpreter (yes, a freaking interpreter!), were written by [Dr. Steve Tate](https://www.uncg.edu/cmp/faculty/srtate/index.html).

The code is first split into lexical tokens and parsed down to an abstract syntax tree. This is then converted to intermediate code in the form of a modified three-address-code schema. Finally, we compile to MIPS, using the QTSpim "standard" (as the MARS simulator supports things that QTSpim does not, and vice versa such as a few pseudo-operations).

*Warning*: The interpreter and compiler generate two similar, but sometimes different results depending on the case. Most of the time, the results are identical. However, edge cases do exist, and not everything has been accounted for. From what I have tested, you can generally *trust* the results of the MIPS output, but the interpreter handles more cases (i.e. the quicksort implementation).

## Running the Compiler
First, download the provided .JAR file. Run it as normal, and type ```littlec``` to view a list of possible commands. You may either type the code into a ```.lc``` file and provide it as an argument to one of the available subcommands, or you may feed in code via standard input.

## Using the Compiler
There are a few differences between C and LittleC, which may be apparent as soon as you start to write a program and get stuck, asking yourself "who the hell made this?!" The following is a list of differences. Note that this does not include omissions; rather things that are present in both languages to a degree, but are modified in LittleC.

1. The ```main``` function no longer accepts command-line/terminal arguments, and does not return a value. Thus, the main method's signature is ```void main()```.
2. All variables must be declared *above* any expressions. So, if you want to declare variables later in the program, you need to either declare them outright at the start, or use a *block*, as you would in C with braces {}.
3. ```for``` loops, and other such statements (```if```, ```while```), do not allow for variable declarations, unless it's inside a new block as described above (this means that, including the statement body,there must be a new block declared). This is similar to C89.
4. The ```extern``` and ```static``` keywords are present, but serve no meaningful purpose, for now at least.
5. Array sizes must be computed at compile time, meaning that all arrays must be declared with an integer literal size.
6. When passing an array to a function, use the signature ```char/int[] s``` instead of ```char/int s[]```, meaning the brackets come *before* the variable name.
7. Initializing string literals in a local block is not directly supported, but is technically allowed by the compiler. Meaning that, if you want to initialize a local string, use the built-in function ```strinit(char[] dest, char[] src)``` (coming soon!).
8. Variables must be declared with a literal value and not an expression (this includes function calls, and everything else that is not a literal).
9. You cannot declare a variable with a negative value, as this is an expression. To use negatives, initialize the value, then assign the negative value to it.

## Features
Most standard programming language concepts are present. These include
- Functions (including prototypes)
- Integers and Character Variables (with 0x and 0b prefixes)
- Arrays (Strings are char arrays as in C)
- Recursion
- Conditionals (Short-Circuiting)
- Loops
- Bitwise XOR, OR, AND, Negation, and Shifting.

## Planned Features
The following is a list of planned features:
- Single & Double Floating-Point Precision Variables
- Power (double asterisk) operator as in Python
- Standard API
- Terminal argument support.
- Support for compiling down to "MARS MIPS"
- ...?


## Dependencies

This project uses Maven, and was developed using Eclipse. Though, it works with any IDE, so long as the ANTLR plugin is available. 

## Reporting Bugs

See the Issues Tab.

## Version History
The **master** branch encompasses all changes. The **development** branches have in-progress additions and updates that are not yet ready for the master branch. There will most likely be other branches present in the future, each with the "development" prefix, and a suffix denoting its purpose with a hyphen (-).
