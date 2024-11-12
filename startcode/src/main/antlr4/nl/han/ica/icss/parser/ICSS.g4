grammar ICSS;

//--- LEXER: ---

// IF support:
IF: 'if';
ELSE: 'else';
BOX_BRACKET_OPEN: '[';
BOX_BRACKET_CLOSE: ']';

// Literals
TRUE: 'TRUE';
FALSE: 'FALSE';
PIXELSIZE: [0-9]+ 'px';
PERCENTAGE: [0-9]+ '%';
SCALAR: [0-9]+;

// Color value takes precedence over id idents
COLOR: '#' [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f];

// Specific identifiers for id's and css classes
ID_IDENT: '#' [a-z0-9\-]+;
CLASS_IDENT: '.' [a-z0-9\-]+;

// General identifiers
LOWER_IDENT: [a-z] [a-z0-9\-]*;
CAPITAL_IDENT: [A-Z] [A-Za-z0-9_]*;

// All whitespace is skipped
WS: [ \t\r\n]+ -> skip;

OPEN_BRACE: '{';
CLOSE_BRACE: '}';
SEMICOLON: ';';
COLON: ':';
PLUS: '+';
MIN: '-';
MUL: '*';
ASSIGNMENT_OPERATOR: ':=';

//--- PARSER: ---
stylesheet: (stylerule | variableAssignment)*;

stylerule: selector OPEN_BRACE styleOption* CLOSE_BRACE;

selector: idSelector | classSelector | tagSelector;

idSelector: ID_IDENT;
classSelector: CLASS_IDENT;
tagSelector: LOWER_IDENT;

declaration: propertyName COLON propertyValue SEMICOLON;

styleOption: declaration | variableAssignment | ifClause;

propertyValue: literals | variableReference | expression;

propertyName: 'color' | 'background-color' | 'width' | 'height';

variableAssignment: variableReference ASSIGNMENT_OPERATOR propertyValue  SEMICOLON;

variableReference: CAPITAL_IDENT;

literals: boolLiteral | colorLiteral | pixelLiteral | percentageLiteral | scalarLiteral;

boolLiteral: TRUE | FALSE;
colorLiteral: COLOR;
pixelLiteral: PIXELSIZE;
percentageLiteral: PERCENTAGE;
scalarLiteral: SCALAR;

expression: expression MUL expression | expression (PLUS | MIN) expression | types;

types: literals | variableReference;

ifClause: IF BOX_BRACKET_OPEN ifExpression BOX_BRACKET_CLOSE OPEN_BRACE styleOption* CLOSE_BRACE elseClause?;

elseClause: ELSE OPEN_BRACE styleOption* CLOSE_BRACE;

ifExpression: variableReference | boolLiteral;