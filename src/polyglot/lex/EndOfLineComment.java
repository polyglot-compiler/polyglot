package jltools.lex;

class EndOfLineComment extends Comment {
  
  EndOfLineComment( int lineNumber, String comment) 
  { 
    super( lineNumber);
    appendLine(comment); 
  }

}
