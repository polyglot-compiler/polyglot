package jltools.lex;


import jltools.util.ErrorQueue;
import jltools.util.ErrorInfo;

import java.io.Reader;
import java.io.LineNumberReader;

/* Java lexer.
 * Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
 * This program is released under the terms of the GPL; see the file
 * COPYING for more details.  There is NO WARRANTY on this code.
 */

public class Lexer {
  LineNumberReader reader;
  boolean isJava12;
  String line = null;
  int line_pos = 1;
  int line_num = 0;
  LineList lineL = new LineList(-line_pos, null); // sentinel for line #0
  ErrorQueue eq;

  public Lexer( Reader reader, ErrorQueue eq) {
    // by default, use a Java 1.2-compatible lexer.
    this( reader, true, eq);
  }
  public Lexer( Reader reader, boolean isJava12, ErrorQueue eq) {
    this.reader = new LineNumberReader(new EscapedUnicodeReader(reader));
    this.isJava12 = isJava12;
    this.eq = eq;
  }

  /* public java_cup.runtime.Symbol nextToken() throws java.io.IOException { */
  public Token nextToken() throws java.io.IOException {
    /* tokens are:
     *  Identifiers/Keywords/true/false/null (start with java letter)
     *  numeric literal (start with number)
     *  character literal (start with single quote)
     *  string (start with double quote)
     *  separator (parens, braces, brackets, semicolon, comma, period)
     *  operator (equals, plus, minus, etc)
     *  whitespace
     *  comment (start with slash)
     */
    InputElement ie;
    int startpos, endpos;
    do {
      startpos = lineL.head + line_pos;
      ie = getInputElement();
      if (ie instanceof DocumentationComment)
	comment = ((Comment)ie).getComment();
    } while (!(ie instanceof Token && ie != null));
    endpos = lineL.head + line_pos;

    //System.out.println(ie.toString()); // uncomment to debug lexer.
    java_cup.runtime.Symbol sym = ((Token)ie).symbol();
    // fix up left/right positions.
    sym.left = startpos; sym.right = endpos;
    // return token.

    /* return jltools.parse.sym; */
    return (Token)ie;
  }
  public boolean debug_lex() throws java.io.IOException {
    InputElement ie = getInputElement();
    System.out.println(ie);
    return !(ie instanceof EOF);
  }

  String comment;
  public String lastComment() { return comment; }
  public void clearComment() { comment=""; }

  InputElement getInputElement() throws java.io.IOException {
    if (line_num == 0)
      nextLine();
    if (line==null)
      return new EOF(line_num);
    if (line.length()<=line_pos) {      // end of line.
      nextLine();
      if (line==null)
	return new EOF(line_num);
    }

    switch (line.charAt(line_pos)) {

      // White space:
    case ' ':	// ASCII SP
    case '\t':	// ASCII HT
    case '\f':	// ASCII FF
    case '\n':	// LineTerminator
      return new WhiteSpace(consume());

      // EOF character:
    case '\020': // ASCII SUB
      consume();
      return new EOF(line_num);

      // Comment prefix:
    case '/':
      return getComment();

      // else, a Token
    default:
      return getToken();
    }
  }
  // May get Token instead of Comment.
  InputElement getComment() throws java.io.IOException {
    String comment;
    // line.charAt(line_pos+0) is '/'
    switch (line.charAt(line_pos+1)) {
    case '/': // EndOfLineComment
      comment = line.substring(line_pos+2);
      line_pos = line.length();
      return new EndOfLineComment( line_num, comment);
    case '*': // TraditionalComment or DocumentationComment
      line_pos += 2;
      if (line.charAt(line_pos)=='*') { // DocumentationComment
	return snarfComment(new DocumentationComment( line_num));
      } else { // TraditionalComment
	return snarfComment(new TraditionalComment( line_num));
      }
    default: // it's a token, not a comment.
      return getToken();
    }
  }

  Comment snarfComment(Comment c) throws java.io.IOException {
    StringBuffer text=new StringBuffer();
    while(true) { // Grab CommentTail
      while (line.charAt(line_pos)!='*') { // Add NotStar to comment.
	int star_pos = line.indexOf('*', line_pos);
	if (star_pos<0) {
	  text.append(line.substring(line_pos));
	  c.appendLine(text.toString()); text.setLength(0);
	  nextLine();
	  if (line==null) {
            eq.enqueue( ErrorInfo.LEXICAL_ERROR, 
                                         "Unterminated comment.", 
                                         c.getLineNumber());
            return null;
          }
	} else {
	  text.append(line.substring(line_pos, star_pos));
	  line_pos=star_pos;
	}
      }
      // At this point, line.charAt(line_pos)=='*'
      // Grab CommentTailStar starting at line_pos+1.
      if (line.charAt(line_pos+1)=='/') { // safe because line ends with '\n'
	c.appendLine(text.toString()); line_pos+=2; return c;
      }
      text.append(line.charAt(line_pos++)); // add the '*'
    }
  }

  Token getToken() {
    // Tokens are: Identifiers, Keywords, Literals, Separators, Operators.
    switch (line.charAt(line_pos)) {
      // Separators: (period is a special case)
    case '(':
    case ')':
    case '{':
    case '}':
    case '[':
    case ']':
    case ';':
    case ',':
      return new Separator(line_num, consume());

      // Operators:
    case '=':
    case '>':
    case '<':
    case '!':
    case '~':
    case '?':
    case ':':
    case '&':
    case '|':
    case '+':
    case '-':
    case '*':
    case '/':
    case '^':
    case '%':
      return getOperator();
    case '\'':
      return getCharLiteral();
    case '\"':
      return getStringLiteral();

      // a period is a special case:
    case '.':
      if (Character.digit(line.charAt(line_pos+1),10)!=-1)
	return getNumericLiteral();
      else return new Separator(line_num, consume());

    default:
      break;
    }
    if (Character.isJavaIdentifierStart(line.charAt(line_pos)))
      return getIdentifier();
    if (Character.isDigit(line.charAt(line_pos)))
      return getNumericLiteral();
    eq.enqueue( ErrorInfo.LEXICAL_ERROR,
                           "Illegal character.",
                           line_num);
    return new EOF( line_num);
  }

  static final String[] keywords = new String[] {
    "abstract", "boolean", "break", "byte", "case", "catch", "char",
    "class", "const", "continue", "default", "do", "double", "else",
    "extends", "final", "finally", "float", "for", "goto", "if",
    "implements", "import", "instanceof", "int", "interface", "long",
    "native", "new", "package", "private", "protected", "public",
    "return", "short", "static", "strictfp", "super", "switch",
    "synchronized", "this", "throw", "throws", "transient", "try", "void",
    "volatile", "while" };
  Token getIdentifier() {
    // Get id string.
    StringBuffer sb = new StringBuffer().append(consume());

    if (!Character.isJavaIdentifierStart(sb.charAt(0))) {
      eq.enqueue( ErrorInfo.LEXICAL_ERROR,
                             "Invalid identifier.",
                             line_num);
      return null;
    }
    while (Character.isJavaIdentifierPart(line.charAt(line_pos)))
      sb.append(consume());
    String s = sb.toString();
    // Now check against boolean literals and null literal.
    if (s.equals("null")) return new NullLiteral(line_num);
    if (s.equals("true")) return new BooleanLiteral(line_num, true);
    if (s.equals("false")) return new BooleanLiteral(line_num, false);
    // Check against keywords.
    //  pre-java 1.2 compatibility:
    if (!isJava12 && s.equals("strictfp")) return new Identifier(line_num, s);
    // use binary search.
    for (int l=0, r=keywords.length; r > l; ) {
      int x = (l+r)/2, cmp = s.compareTo(keywords[x]);
      if (cmp < 0) r=x; else l=x+1;
      if (cmp== 0) return new Keyword(line_num, s);
    }
    // not a keyword.
    return new Identifier(line_num, s);
  }
  NumericLiteral getNumericLiteral() {
    int i;
    // leading decimal indicates float.
    if (line.charAt(line_pos)=='.')
      return getFloatingPointLiteral();
    // 0x indicates Hex.
    if (line.charAt(line_pos)=='0' &&
	(line.charAt(line_pos+1)=='x' ||
	 line.charAt(line_pos+1)=='X')) {
      line_pos+=2; return getIntegerLiteral(/*base*/16);
    }
    // otherwise scan to first non-numeric
    for (i=line_pos; Character.digit(line.charAt(i),10)!=-1; )
      i++;
    switch(line.charAt(i)) { // discriminate based on first non-numeric
    case '.':
    case 'f':
    case 'F':
    case 'd':
    case 'D':
    case 'e':
    case 'E':
      return getFloatingPointLiteral();
    case 'L':
    case 'l':
    default:
      if (line.charAt(line_pos)=='0')
	return getIntegerLiteral(/*base*/8);
      return getIntegerLiteral(/*base*/10);
    }
  }
  NumericLiteral getIntegerLiteral(int radix) {
    long val=0;
    while (Character.digit(line.charAt(line_pos),radix)!=-1)
      val = (val*radix) + Character.digit(consume(),radix);
    if (line.charAt(line_pos) == 'l' ||
	line.charAt(line_pos) == 'L') {
      consume();
      return new LongLiteral(line_num, val);
    }
    // we compare MAX_VALUE against val/2 to allow constants like
    // 0xFFFF0000 to get past the test. (unsigned long->signed int)
    if ((val/2) > Integer.MAX_VALUE ||
        val    < Integer.MIN_VALUE) {
      eq.enqueue( ErrorInfo.LEXICAL_ERROR,
                             "Integer literal greater than Integer.MAX_VALUE",
                             line_num);
      return null;
    }
    return new IntegerLiteral(line_num, (int)val);
  }
  NumericLiteral getFloatingPointLiteral() {
    String rep = getDigits();
    if (line.charAt(line_pos)=='.')
      rep+=consume() + getDigits();
    if (line.charAt(line_pos)=='e' ||
	line.charAt(line_pos)=='E') {
      rep+=consume();
      if (line.charAt(line_pos)=='+' ||
	  line.charAt(line_pos)=='-')
	rep+=consume();
      rep+=getDigits();
    }
    try {
      switch (line.charAt(line_pos)) {
      case 'f':
      case 'F':
	consume();
	return new FloatLiteral(line_num, Float.valueOf(rep).floatValue());
      case 'd':
      case 'D':
	consume();
	/* falls through */
      default:
	return new DoubleLiteral(line_num, Double.valueOf(rep).doubleValue());
      }
    } catch (NumberFormatException e) {
      eq.enqueue( ErrorInfo.LEXICAL_ERROR,
                             "Invalid floating-point literal.",
                             line_num);
      return null;
    }
  }
  String getDigits() {
    StringBuffer sb = new StringBuffer();
    while (Character.digit(line.charAt(line_pos),10)!=-1)
      sb.append(consume());
    return sb.toString();
  }

  Operator getOperator() {
    char first = consume();
    char second= line.charAt(line_pos);

    switch(first) {
      // single-character operators.
    case '~':
    case '?':
    case ':':
      return new Operator(line_num, new String(new char[] {first}));
      // doubled operators
    case '+':
    case '-':
    case '&':
    case '|':
      if (first==second)
	return new Operator(line_num, new String(new char[] {first, consume()}));
    default:
      break;
    }
    // Check for trailing '='
    if (second=='=')
	return new Operator(line_num, new String(new char[] {first, consume()}));

    // Special-case '<<', '>>' and '>>>'
    if ((first=='<' && second=='<') || // <<
	(first=='>' && second=='>')) {  // >>
      String op = new String(new char[] {first, consume()});
      if (first=='>' && line.charAt(line_pos)=='>') // >>>
	op += consume();
      if (line.charAt(line_pos)=='=') // <<=, >>=, >>>=
	op += consume();
      return new Operator(line_num, op);
    }

    // Otherwise return single operator.
    return new Operator(line_num, new String(new char[] {first}));
  }

  CharacterLiteral getCharLiteral() {
    char firstquote = consume();
    char val;
                                  
    switch (line.charAt(line_pos)) {
    case '\\':
      val = getEscapeCharacter();
      break;
    case '\'':
      eq.enqueue( ErrorInfo.LEXICAL_ERROR,
                             "Invalid character literal.",
                             line_num);
      return null;
    case '\n':
      eq.enqueue( ErrorInfo.LEXICAL_ERROR,
                             "Invalid character literal.",
                             line_num);
      return null;
    default:
      val = consume();
      break;
    }

    char secondquote = consume();
    if (firstquote != '\'' || secondquote != '\'') {
      eq.enqueue( ErrorInfo.LEXICAL_ERROR,
                             "Invalid character literal.",
                             line_num);
      return null;
    }
    return new CharacterLiteral(line_num, val);
  }

  StringLiteral getStringLiteral() {
    char openquote = consume();
    StringBuffer val = new StringBuffer();

    while (line.charAt(line_pos)!='\"') {
      switch(line.charAt(line_pos)) {
      case '\\':
        val.append(getEscapeCharacter());
        break;
      case '\n':
        eq.enqueue( ErrorInfo.LEXICAL_ERROR,
                               "String literals may not contain new lines.",
                               line_num);
        return null;
      default:
        val.append(consume());
        break;
      }
    }
    char closequote = consume();
    if (openquote != '\"' || closequote != '\"') {
      eq.enqueue( ErrorInfo.LEXICAL_ERROR,
                             "Invalid string literal.",
                             line_num);
      return null;
    }
    return new StringLiteral(line_num, val.toString().intern());
  }

  String getEscapeSequence() {
    if (consume() != '\\') {
      eq.enqueue( ErrorInfo.LEXICAL_ERROR,
                             "Invalid escape sequence.",
                             line_num);
      return null;
    }
    switch(line.charAt(line_pos)) {
    case 'b':
      consume(); return "\\b";
    case 't':
      consume(); return "\\t";
    case 'n':
      consume(); return "\\n";
    case 'f':
      consume(); return "\\f";
    case 'r':
      consume(); return "\\r";
    case '\"':
      consume(); return "\\\"";
    case '\'':
      consume(); return "\\'";
    case '\\':
      consume(); return "\\\\";
    case '0':
    case '1':
    case '2':
    case '3':
      return getOctalEscapeSequence(3);
    case '4':
    case '5':
    case '6':
    case '7':
      return getOctalEscapeSequence(2);
    default:
      eq.enqueue( ErrorInfo.LEXICAL_ERROR,
                             "Invalid escape sequence.",
                             line_num);
      return null;
    }
  }

  char getEscapeCharacter() {
    if (consume() != '\\') {
      eq.enqueue( ErrorInfo.LEXICAL_ERROR,
                             "Invalid escape sequence.",
                             line_num);
      return 0;
    }
    switch(line.charAt(line_pos)) {
    case 'b':
      consume(); return '\b';
    case 't':
      consume(); return '\t';
    case 'n':
      consume(); return '\n';
    case 'f':
      consume(); return '\f';
    case 'r':
      consume(); return '\r';
    case '\"':
      consume(); return '\"';
    case '\'':
      consume(); return '\'';
    case '\\':
      consume(); return '\\';
    case '0':
    case '1':
    case '2':
    case '3':
      return getOctalEscapeCharacter(3);
    case '4':
    case '5':
    case '6':
    case '7':
      return getOctalEscapeCharacter(2);
    default:
      eq.enqueue( ErrorInfo.LEXICAL_ERROR,
                             "Invalid escape sequence.",
                             line_num);
      return 0;
    }
  }

  String getOctalEscapeSequence(int maxlength) {
    String result = "\\";
    char digit;
    for (int i=0; i<maxlength; i++)
    {
      digit = consume();
      if( !isOctalDigit(digit)) {
        eq.enqueue( ErrorInfo.LEXICAL_ERROR,
                               "Invalid octal escape sequence.",
                               line_num);
        return null;
      }
      result += digit;
    }
    return result;
  }

  char getOctalEscapeCharacter( int maxlength) {
    int val = 0;
    int i;
    for( i = 0; i < maxlength; i++) {
      if( Character.digit( line.charAt( line_pos), 8) != -1) {
        val = (8 * val) + Character.digit( consume(), 8);
      }
      else {
        break;
      }
    }
    if( (i == 0) || (val > 0xFF)) {
      eq.enqueue( ErrorInfo.LEXICAL_ERROR,
                             "Invalid octal escape sequence.",
                             line_num);
      return 0;
    }
    return (char)val;
  }

  boolean isOctalDigit(char c)
  {
    return (Character.isDigit(c) &&
            Character.digit(c, 10) < 8);
  }

  char consume() { return line.charAt(line_pos++); }
  void nextLine() throws java.io.IOException {
    line=reader.readLine();
    if (line!=null) line=line+'\n';
    lineL = new LineList(lineL.head+line_pos, lineL); // for error reporting
    line_pos=0;
    line_num++;
  }

  // Deal with error messages.
  /*
  public void errorMsg(String msg, java_cup.runtime.Symbol info) {
    int n=line_num, c=info.left-lineL.head;
    for (LineList p = lineL; p!=null; p=p.tail, n--)
	if (p.head<info.left) { c=info.left-p.head; break; }
    System.err.println(msg+" at line "+n);
  }
  */

  class LineList {
    int head;
    LineList tail;
    LineList(int head, LineList tail) { this.head = head; this.tail = tail; }
  }
}
