package jltools.lex;

class EndOfLineComment extends Comment {
  EndOfLineComment(String comment) { appendLine(comment); }
}
