import java.util.regex.PatternSyntaxException;

%%// Options of the scanner

%class LexicalAnalyzer // Name
%unicode               // Use unicode
%line                  // Use line counter (yyline variable)
%column                // Use character counter by line (yycolumn variable)
%function nextToken
%type Symbol
%yylexthrow PatternSyntaxException

%eofval{
	return new Symbol(LexicalUnit.EOS, yyline, yycolumn);
%eofval}

//Extended Regular Expressions

AlphaUpperCase    = [A-Z]
AlphaLowerCase    = [a-z]
Alpha             = {AlphaUpperCase}|{AlphaLowerCase}
Numeric           = [0-9]
AlphaNumeric      = {Alpha}|{Numeric}
LowerAlphaNumeric	= {AlphaLowerCase}|{Numeric}

BadNumber     = (0[0-9]+)
Number        = ([1-9][0-9]*)|0
VarName        = ({AlphaLowerCase})({LowerAlphaNumeric})*
ProgName       = ({AlphaUpperCase})(({AlphaNumeric})*({AlphaLowerCase})({AlphaNumeric})* | {Numeric}*)
BadProgName    = ({AlphaUpperCase})+
LineFeed       = "\n"
CarriageReturn = "\r"
EndLine        = ({LineFeed}{CarriageReturn}?) | ({CarriageReturn}{LineFeed}?)
Space          = (\t | \f | " ")
Spaces         = {Space}+

//Declare exclusive states
%xstate YYINITIAL, SHORTCOMMENTS, LONGCOMMENTS

%%// Identification of tokens

<SHORTCOMMENTS> {
// End of comment
{EndLine}        {yybegin(YYINITIAL);} // go back to analysis
.	   				     {} //ignore any character
}

<LONGCOMMENTS> {
// End of comment
	"%%"             {yybegin(YYINITIAL);} // go back to analysis
  <<EOF>>          {throw new PatternSyntaxException("A comment is never closed.",yytext(),yyline);}
	[^]					     {} //ignore any character
}

<YYINITIAL> {
// Comments
    "::"              {yybegin(SHORTCOMMENTS);} // go to ignore mode
    "%%"              {yybegin(LONGCOMMENTS);} // go to ignore mode
// Delimiters
  "BEGIN"             {return new Symbol(LexicalUnit.BEGIN, yyline, yycolumn, yytext());}
  "END"               {return new Symbol(LexicalUnit.END, yyline, yycolumn, yytext());}
  ","                 {return new Symbol(LexicalUnit.COMMA, yyline, yycolumn, yytext());}
// Assignation
  ":="                {return new Symbol(LexicalUnit.ASSIGN, yyline, yycolumn, yytext());}
// Parenthesis
  "("                 {return new Symbol(LexicalUnit.LPAREN, yyline, yycolumn, yytext());}
  ")"                 {return new Symbol(LexicalUnit.RPAREN, yyline, yycolumn, yytext());}
// Arithmetic signs
  "+"                 {return new Symbol(LexicalUnit.PLUS, yyline, yycolumn, yytext());}
  "-"                 {return new Symbol(LexicalUnit.MINUS, yyline, yycolumn, yytext());}
  "*"                 {return new Symbol(LexicalUnit.TIMES, yyline, yycolumn, yytext());}
  "/"                 {return new Symbol(LexicalUnit.DIVIDE, yyline, yycolumn, yytext());}
// Conditional keywords
  "IF"                {return new Symbol(LexicalUnit.IF, yyline, yycolumn, yytext());}
  "THEN"              {return new Symbol(LexicalUnit.THEN, yyline, yycolumn, yytext());}
  "ELSE"              {return new Symbol(LexicalUnit.ELSE, yyline, yycolumn, yytext());}
// Loop keywords
  "WHILE"             {return new Symbol(LexicalUnit.WHILE, yyline, yycolumn, yytext());}
  "DO"                {return new Symbol(LexicalUnit.DO, yyline, yycolumn, yytext());}
// Comparison operators
  "="                 {return new Symbol(LexicalUnit.EQUAL, yyline, yycolumn, yytext());}
  ">"                 {return new Symbol(LexicalUnit.GREATER, yyline, yycolumn, yytext());}
  "<"                 {return new Symbol(LexicalUnit.SMALLER, yyline, yycolumn, yytext());}
// IO keywords
  "PRINT"             {return new Symbol(LexicalUnit.PRINT, yyline, yycolumn, yytext());}
  "READ"              {return new Symbol(LexicalUnit.READ, yyline, yycolumn, yytext());}
// Numbers
  {BadNumber}        {System.err.println("Warning! Numbers with leading zeros are not permitted: " + yytext()); return new Symbol(LexicalUnit.NUMBER, yyline, yycolumn, Integer.valueOf(yytext()));}
  {Number}           {return new Symbol(LexicalUnit.NUMBER, yyline, yycolumn, Integer.valueOf(yytext()));}
// Variable Names
  {VarName}           {return new Symbol(LexicalUnit.VARNAME, yyline, yycolumn, yytext());}
// Program Names
  {ProgName}          {return new Symbol(LexicalUnit.PROGNAME, yyline, yycolumn, yytext());}
  {BadProgName}       {System.err.println("Warning! Program names in uppercase are not permitted: " + yytext()); return new Symbol(LexicalUnit.PROGNAME, yyline, yycolumn, yytext());}
// Other
  {Spaces}	          {} // ignore spaces
  {EndLine}           {} // ignore endlines
  [^]                 {throw new PatternSyntaxException("Unmatched symbol(s) found.",yytext(),yyline);} // unmatched symbols gives an error
}
