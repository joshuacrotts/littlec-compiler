
## Syntax Trees

After parsing, the input LittleC program is represented by a syntax
tree, which can have a variety of kinds of nodes that represent the
structure of a program. Your program should be able to create and
print syntax trees, and in order for testing to work your output must
follow a fairly strict format, which is defined here.

The basic ideas can be understood from a simple example. Consider the
following snippet of LittleC code:

```
    xsq = x*x;
    y = xsq + 2*x +1;
```

This is a sequence of two assignment statements, each of which is
evaluated using various expression operators. This code would produce
the following syntax tree -- each line represents a node in the syntax
tree, and each node has a mandatory type (even if it is `void`), a
label, possible additional information (see the ID nodes), and
children listed between parentheses. This output also indents to
represent the tree structure "pretty-printing" style, but this isn't
actually required (it sure makes it easy to read though!). Read
through this carefully and make sure you understand how this
represents the code above.


```
    void SEQ (
      int ASN (
        int ID xsq,
        int BINOP('*') (
          int ID x,
          int ID x)),
      int ASN (
        int ID y,
        int BINOP('+') (
          int BINOP('+') (
            int ID xsq,
            int BINOP('*') (
              int LIT = 2,
              int ID x)),
          int LIT = 1)))
```

The full list of syntax tree labels, types, and other information is
given below. You must use *exactly* the following format when printing
a node in your syntax tree:

* First the node type followed by a space
* Then the node label followed by a space
* Then any "Other node info" specified for that type of node
* Then children surrounded by parentheses and separated by commas. If
  there is the *possibility* of a kind of node having children, you
  must print the parentheses, even if there are no children. In other
  words, a function call with no arguments would still include the
  child list as `()`.

There is no requirement on spacing or line breaks, other than to
separate pieces of the output. The formatting is entirely for your
benefit when examining the output.

### Types

You will need to print out types associated with nodes in your syntax
tree (and for variable declarations, function signatures, etc.).

* Integer types: `int` and `char` -- the only difference between these
  two types is their size (32 bits vs 8 bits), but otherwise can be
  used in the same ways.

* Special type: `void` -- represents the type of a function that
  doesn't return a value, or a syntax tree node that doesn't represent
  a value-producing expression

* Array reference types: `char[]` and `int[]` -- represent variables
  that refer to arrays, but which do not actually allocate the storage
  for the arrays themselves. This is particularly useful as a
  parameter type in order to pass in a reference to an array.

* Array storage types: Represent an array which has allocated storage,
  and the type includes the length of the array. Examples of array
  storage types include `char[10]` and `int[45]`.

### Full List of Syntax Tree Nodes

This section gives a complete list of syntax tree nodes, and the
information that should be printed with a node when it is
output. Remember that if there is a *possibility* of a node having
children, you should print the open and closed parentheses for the
children, even if there aren't any children (e.g., in a function
call's possibly-empty argument list).

#### Nodes associated with expressions

All of these nodes can appear in an expression, and correspond to
values that will be computed by the program. Because of this, the type
of each of these nodes reflects the type of the value being computed.
Not all node types need to be implemented and supported for CSC 439
Assignment 2, but will need to be supported in Assignment 3 -- see the
assignment statements for more information.


* Variable/Identifier node
  * Label: ID
  * Type: The type of the variable
  * Other node info: The name of the variable
  * Children: None
  
* Constant/literal value node
  * Label: LIT = "+value;
  * Type: Type of the literal (will be int, char, or char[])
  * Other node info: The literal value
  * Children: None

* Array indexing node
  * Label: AIDX
  * Type: The type of one of the array elements
  * Other node info: None
  * Children: Two children
    * Child 1: The array identifier (as a syntax tree)
    * Child 2: The expression for the index
  
* Assignment node
  * Label: ASN
  * Type: The type of the value that was assigned 
  * Other node info: None
  * Children: Always 2 children
    * Child 1: the left-hand-side (an lvalue - it may not be be just an ID)
    * Child 2: the right-hand-side expression for the value to store
    
* Binary operator node - *actualop* below is replaced with operator string (e.g., BINOP(+))
  * Label: BINOP(*actualop*)
  * Type: The type of the value produced when this operation is performed
  * Other node info: None
  * Children: Always 2 children
    * Child 1: The left operand
    * Child 2: The right operand
  
* Unary operator node - *actualop* below is replaced with operator string (e.g., UNARYOP(-))
  * Label: UNARYOP(*actualop*)
  * Type: The type of the value produced when this operation is performed
  * Other node info: None
  * Children: Always 1 child, for the one operand of the unary operator

* Pre/Post Increment/Decrement nodes
  * Label: One of four different possibilities, PRE-INC, PRE-DEC,
    POST-INC, or POST-DEC
  * Type: Type of the lvalue being incremented/decremented
  * Other node info: None
  * Children: One child
    * Child 1: The lvalue being incremented/decremented
  
* Function call node
  * Label: FNCALL
  * Type: The return type of the function being called
  * Other node info: Name of the function being called
  * Children: 0 or more for the arguments passed to the function call
    -- always include children "()" in output, even if no
    arguments/children

* Type casting node
  * Label: CAST
  * Type: Target type (type being cast to)
  * Other node info: None
  * Children: One child for the value being cast


#### Non-expression nodes (all have type void)

These represent statements, including variable declarations. They
don't produce values, so can't be used in expressions, and so each of
these syntax tree nodes has type void.

* Sequence of syntax trees
  * Label: SEQ
  * Type: void
  * Other node info: None
  * Children: 0 or more - each child is an item in a sequence of items
    (declarations, statements, etc.)

* Function definition node
  * Label: FNDEF
  * Type: void
  * Other node info: Function name followed by its signature
  * Children: One child for the body of the function

* Parameter declaration node (note: different from variable declaration)
  * Label: PDECL
  * Type: void
  * Other node info: The name of the identifier being declared followed by its
    type in parentheses
  * Children: None

* Variable declaration node
  * Label: DECL
  * Type: void
  * Other node info: The name of the identifier being declared, its
    type in parentheses, and an optional initializer written as " =
    *literal*"
  * Children:  None

* Break statement node
  * Label: BREAK
  * Type: void
  * Other node info: None
  * Children: None
  
* Return statement node
  * Label: RETURN
  * Type: void
  * Other node info: None
  * Children: 0 or 1, giving the optional return value

* If statement node
  * Label: IF
  * Type: void
  * Other node info: None
  * Children: 2 or 3
    * Child 1: condition
    * Child 2: then part
    * Child 3: (optional) else part
  
* While statement node
  * Label: WHILE
  * Type: void
  * Other node info: None
  * Children: Always two children
    * Child 1: The loop condition
    * Child 2: The body of the loop
  
### For Loops

You'll notice that there is no syntax tree node for a for loop. This
is not a mistake! A for loop is just a "fancy while loop", so a for
loop will just use the SEQ and WHILE node types to create the for
loop. In particular, a for loop has three parts listed at the top: a
loop initialization expression, a loop test expression, and a loop
update expression. The syntax tree should put the initializer and a
while loop in a sequence (which includes the loop test), and then puts
the loop body in a sequence with the loop update at the end. Here's a
simple example that prints numbers from 1 to 10:

```
   for (i = 1; i <= 10; i++)
      printd(i);
```

Results in this syntax tree:

```
      void SEQ (
        int ASN (
          int ID i,
          int LIT = 1),
        void WHILE (
          int BINOP('<=') (
            int ID i,
            int LIT = 10),
          void SEQ (
            void FNCALL printd (
              int ID i),
            int POST-INC (
              int ID i))))
```

Make sure you match the sequence nesting structure given here, so your
code will pass the required tests.


