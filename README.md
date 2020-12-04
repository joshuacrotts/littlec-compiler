<h1 align="center">LittleC Compiler</h1>

[![CodeFactor](https://www.codefactor.io/repository/github/joshuacrotts/LittleC-Compiler/badge)](https://www.codefactor.io/repository/github/joshuacrotts/LittleC-Compiler) ![GitHub contributors](https://img.shields.io/github/contributors/JoshuaCrotts/LittleC-Compiler) ![GitHub commit activity](https://img.shields.io/github/commit-activity/m/JoshuaCrotts/LittleC-Compiler) ![GitHub repo size](https://img.shields.io/github/repo-size/JoshuaCrotts/LittleC-Compiler) [![GitHub issues open](https://img.shields.io/github/issues/JoshuaCrotts/LittleC-Compiler)]() 
[![GitHub issues closed](https://img.shields.io/github/issues-closed-raw/JoshuaCrotts/LittleC-Compiler)]()

LittleC is a small, C-like language written for my compilers course. Some segments of the code, including the interpreter (yes, a freaking interpreter!), were written by [Dr. Steve Tate](https://www.uncg.edu/cmp/faculty/srtate/index.html).

The code is first split into lexical tokens and parsed down to an abstract syntax tree. This is then converted to intermediate code in the form of a modified three-address-code schema. Finally, we compile to MIPS, using the QTSpim "standard" (as the MARS simulator supports things that QTSpim does not, and vice versa such as a few pseudo-operations).


## Running the Compiler
First, download the provided .JAR file. Run it as normal, and type "littlec" to view a list of possible commands. You may either type the code into a .lc file and provide it as an argument to one of the available subcommands, or you may feed in code via standard input.

## Features
Most standard programming language concepts are present. These include
- Functions
- Integers and Character Variables
- Arrays (Strings are char arrays as in C)
- Recursion
- Conditionals (Short-Circuiting)
- Loops

## Planned Features
The following is a list of planned features:
- Single & Double Floating-Point Precision Variables
- Bitwise Operators
- Standard API
- Support for compiling down to "MARS MIPS"
- ...?

## Dependencies

This project uses Maven, and was developed using Eclipse. Though, it works with any IDE, so long as the ANTLR plugin is available. 

## Reporting Bugs

See the Issues Tab.

## Version History
The **master** branch encompasses all changes. The **development** branches have in-progress additions and updates that are not yet ready for the master branch. There will most likely be other branches present in the future, each with the "development" prefix, and a suffix denoting its purpose with a hyphen (-).