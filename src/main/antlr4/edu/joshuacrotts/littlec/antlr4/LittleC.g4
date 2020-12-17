grammar LittleC;

//=========== Lexeme patterns and tokens start here ==============

// Put your lexical analyzer rules here - the following rule is just
// so that there is some lexer rule in the initial grammar (otherwise
// ANTLR won't make a Lexer class)


/* Miscellaneous and skippable lexemes. */
WHITESPACE              : [ \r\n\t]+                               -> skip ;
COMMENT                 : '//'(.)*?NEWLINE                         -> skip ; // Match any text that has // preceding.
fragment DIGIT          : [0-9]                                            ;
fragment UPPER_CASE_LTR : [a-z]                                            ;
fragment LOWER_CASE_LTR : [A-Z]                                            ;
fragment ANY_CASE_LTR   : [a-zA-Z]                                         ;
fragment UNDERSCORE     : '_'                                              ;
fragment SINGLE_QUOTE   : '\''                                             ;
fragment DOUBLE_QUOTE   : '"'                                              ;
fragment ANYCHAR        : .                                                ;
fragment NEWLINE        : '\n'                                             ;
fragment CARRIAGE_RET   : '\r'                                             ;
fragment TAB            : '\t'                                             ;
fragment NULL_CHAR      : '\\0'                                            ;
fragment ESCAPED_CHAR   : ('\\' .)                                         ;
fragment ANYCHAR_MOD    : (.+?) ; // Requires at least ONE character, whether it's special or not. If it's an empty char, that's the parser's problem.

/* Fixed string tokens. */
// Storage classes (2).
STATIC         : 'static' ;
EXTERN         : 'extern' ;

// Core types (4).
INT            : 'int'    ;
FLOAT		   : 'float'  ;
CHAR           : 'char'   ;
VOID           : 'void'   ;

// Keywords (7).
IF             	: 'if'     	;
ELSE           	: 'else'   	;
WHILE          	: 'while'  	;
FOR            	: 'for'    	;
RETURN         	: 'return' 	;
BREAK          	: 'break'  	;

/* Rule tokens. */
CHARLIT		   : (SINGLE_QUOTE (ESCAPED_CHAR | ~ ['\\] ) '\'');
STRINGLIT      : (DOUBLE_QUOTE (ESCAPED_CHAR | ~ ["\\] )* '"');
INTLIT         : (DIGIT)+ | ('0x'[0-9A-Fa-f]+) | ('0b'[0|1]+) ;
FLOATLIT	   : (DIGIT)+('.'(DIGIT)*)?						  ;
ID             : (ANY_CASE_LTR | UNDERSCORE)(ANY_CASE_LTR | DIGIT | UNDERSCORE)* ;

// Assignment operator (1).
ASSIGN           : '='    ;

// Integer/array operations (15).
SIZE_OP          : '#'    ;
PLUS_OP          : '+'    ;
MINUS_OP         : '-'    ;
MULTIPLY_OP      : '*'    ;
POW_OP			 : '**'   ;
DIVIDE_OP        : '/'    ;
MODULO_OP        : '%'    ;
INC_OP           : '++'   ;
DEC_OP           : '--'   ;
BIT_AND		     : '&'    ;
BIT_OR 		   	 : '|'    ;
BIT_NEG   		 : '~'    ;
BIT_XOR			 : '^'    ;
BIT_SHIFT_LEFT   : '<<'   ;
BIT_SHIFT_RIGHT  : '>>'   ;


// Comparison operators (6).
EQUAL_CMP        		: '=='   	;
LESS_THAN_CMP    		: '<'    	;
LESS_EQ_CMP      		: '<='   	;
GREATER_THAN_CMP 		: '>'    	;
GREATER_EQ_CMP   		: '>='   	;
NOT_EQUAL_CMP    		: '!='   	;
 
// Boolean operators (3).
NOT : '!'  ;
AND : '&&' ;
OR  : '||' ;

// Punctuation (10).
OPEN_PAREN    : '('  ;
CLOSE_PAREN   :  ')' ;
OPEN_BRACE    : '{'  ;
CLOSE_BRACE   : '}'  ;
OPEN_BRACKET  : '['  ;
CLOSE_BRACKET : ']'  ;
SEMICOLON     : ';'  ;
TERNARY_COND  : '?'  ;
TERNARY_ELSE  : ':'  ;
COMMA 	      : ','  ;

//=========== Grammar starts here ==============

program		        : declaration* stmt* ;
					
					  
// A declaration is just a variable declaration/initialization, prototype, or a fn declaration (with a body!).
declaration         : ((ruleVariableDeclaration | ruleFunctionPrototype) SEMICOLON) 
					| ruleFunctionDeclaration 
					;


// Function-related rules.
ruleFunctionPrototype				: (EXTERN|STATIC)? (INT|CHAR|FLOAT|VOID) ID OPEN_PAREN ruleFunctionDeclarationParameters* CLOSE_PAREN #functionPrototype;
ruleFunctionDeclarationParameters   : (((termDatatype ID COMMA)* (termDatatype ID))) #functionDeclarationParams;
ruleFunctionDeclaration          	: (EXTERN|STATIC)? ((INT|CHAR|FLOAT|VOID) ID OPEN_PAREN ruleFunctionDeclarationParameters* CLOSE_PAREN ruleFunctionBody) #functionDeclaration;
ruleFunctionCallParameters			: ((expr COMMA)* expr) #FunctionCallParameters;
ruleFunctionCall                 	: ID OPEN_PAREN ruleFunctionCallParameters? CLOSE_PAREN #functionCall;
ruleFunctionBody                 	: OPEN_BRACE declaration* stmt* CLOSE_BRACE #functionBody;


// Variable-related rules.
ruleVariableDeclaration		 : ruleIntDeclaration 
							 | ruleIntArrayDeclaration 
							 | ruleIntArrayRefDeclaration 
							 | ruleFloatDeclaration 
							 | ruleFloatArrayDeclaration 
							 | ruleFloatArrayRefDeclaration 
							 | ruleCharDeclaration 
							 | ruleStringDeclaration 
							 | ruleStringRefDeclaration
							 ;
ruleIntDeclaration			 : (EXTERN|STATIC)? INT ID (ASSIGN (INTLIT | CHARLIT))? #IntDeclaration;
ruleIntArrayDeclaration		 : (EXTERN|STATIC)? INT ID OPEN_BRACKET INTLIT CLOSE_BRACKET #IntArrayDeclaration;
ruleIntArrayRefDeclaration	 : (EXTERN|STATIC)? INT OPEN_BRACKET CLOSE_BRACKET ID #IntArrayRefDeclaration;
ruleFloatDeclaration		 : (EXTERN|STATIC)? FLOAT ID (ASSIGN (INTLIT | CHARLIT))? #FloatDeclaration;
ruleFloatArrayDeclaration	 : (EXTERN|STATIC)? FLOAT ID OPEN_BRACKET INTLIT CLOSE_BRACKET #FloatArrayDeclaration;
ruleFloatArrayRefDeclaration : (EXTERN|STATIC)? FLOAT OPEN_BRACKET CLOSE_BRACKET ID #FloatArrayRefDeclaration;
ruleCharDeclaration			 : (EXTERN|STATIC)? CHAR ID (ASSIGN (INTLIT | CHARLIT))? #CharDeclaration;
ruleStringDeclaration		 : (EXTERN|STATIC)? CHAR ID OPEN_BRACKET INTLIT CLOSE_BRACKET (ASSIGN STRINGLIT)? #StringDeclaration;
ruleStringRefDeclaration     : (EXTERN|STATIC)? CHAR OPEN_BRACKET CLOSE_BRACKET ID #StringRefDeclaration;


// Term-related rules.
termDatatype           	: (INT|CHAR|FLOAT)(OPEN_BRACKET (INTLIT|CHARLIT)? CLOSE_BRACKET)?;
// A term is just a literal or a variable.
term                    : INTLIT 
						| CHARLIT 
						| FLOATLIT
						| STRINGLIT 
						| ID
						;


// Tons of expressions ordered from highest to lowest precedence.
expr					: term #exprTerm  // General term (literal or var.)       
						| ruleFunctionCall #exprFunctionCall // Function calls.
						| OPEN_PAREN expr CLOSE_PAREN #exprParen // Parenthesis       
						| ID OPEN_BRACKET expr CLOSE_BRACKET #exprArray // Array dereference.        
						| (INC_OP|DEC_OP) ID (OPEN_BRACKET expr CLOSE_BRACKET)? #exprPreOp	// Pre operators.
						| ID (OPEN_BRACKET expr CLOSE_BRACKET)? (INC_OP|DEC_OP) #exprPostOp // Post operators.
						| (PLUS_OP|MINUS_OP|NOT|BIT_NEG|SIZE_OP) expr #exprUnary // Unary operators.
						| <assoc=right> expr (POW_OP) expr #exprBinaryOp // Power operator.
						| expr (MULTIPLY_OP | DIVIDE_OP | MODULO_OP) expr #exprBinaryOp // Multiply, divide, modulo.
						| expr (PLUS_OP | MINUS_OP) expr #exprBinaryOp // Addition & subtraction.
						| expr (BIT_SHIFT_LEFT | BIT_SHIFT_RIGHT) expr #exprBinaryOp // Bitwise shift left/right.
						| expr (LESS_THAN_CMP | LESS_EQ_CMP | GREATER_THAN_CMP | GREATER_EQ_CMP) expr #exprBinaryOp // Comparison operators.
						| expr (EQUAL_CMP | NOT_EQUAL_CMP) expr #exprBinaryOp // Comparison of equality.
						| expr (BIT_AND) expr #exprBinaryOp // Bitwise AND
						| expr (BIT_XOR) expr #exprBinaryOp // Bitwise XOR
						| expr (BIT_OR) expr #exprBinaryOp // Bitwise OR
						| expr (AND) expr #exprBinaryOp // Comparison of AND.
						| expr (OR) expr #exprBinaryOp // Comparison of OR.
						| <assoc=right> expr TERNARY_COND expr TERNARY_ELSE expr #exprTernaryOp // Ternary operator. 
						| ruleAssignStatement #exprAssign
						; 
                          

stmt			: ruleNewBlock
				| ruleIfStatement 
				| ruleWhileStatement 
				| ruleForStatement 
				| (ruleBreakStatement SEMICOLON) 
				| (ruleAssignStatement SEMICOLON) 
				| (ruleReturnStatement SEMICOLON) 
				| (expr SEMICOLON);                                         

// Declares a new block in the middle of a statement { ... }
ruleNewBlock 			: (OPEN_BRACE declaration* stmt* CLOSE_BRACE) #newBlock;

// Assignment. While it's not a direct "statement", it's used as a statement rule to
// make conditionals easier.
ruleAssignStatement		: <assoc=right> ID (OPEN_BRACKET expr CLOSE_BRACKET)? ASSIGN (expr | term) #assignStatement;

// If statement with else.
ruleIfStatement			: IF OPEN_PAREN ruleIfStatementCond CLOSE_PAREN (stmt | (OPEN_BRACE stmt* CLOSE_BRACE)) ruleElseStatement? #ifStatement;
ruleIfStatementCond		: expr #ifStatementConditional;
ruleElseStatement		: ELSE (stmt | (OPEN_BRACE stmt* CLOSE_BRACE)) #elseStatement;

// While statement.
ruleWhileStatement		: WHILE OPEN_PAREN ruleWhileStatementCond CLOSE_PAREN (stmt | (OPEN_BRACE stmt* CLOSE_BRACE)) #whileStatement;
ruleWhileStatementCond	: expr #whileStatementConditional;

// For statement (same as while semantically).
ruleForStatement		: FOR OPEN_PAREN (expr SEMICOLON) (ruleForStatementCond SEMICOLON) (expr) CLOSE_PAREN (stmt | (OPEN_BRACE stmt* CLOSE_BRACE)) #forStatement;
ruleForStatementCond	: expr #forStatementConditional;

// Return statement.
ruleReturnStatement		: RETURN expr? #returnStatement;

// Break statement.
ruleBreakStatement		: BREAK #breakStatement;
