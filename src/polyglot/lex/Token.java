package jltools.lex;

abstract class Token extends InputElement {
  int line;

  public Token(int lineNumber)
  {
    this.line = line;
  }

  public int getLine()
  {
	return line;
  }

  abstract java_cup.runtime.Symbol symbol();

  protected static String escape(String s) {
    StringBuffer sb = new StringBuffer();
    for (int i=0; i<s.length(); i++)
      switch(s.charAt(i)) {
      case '\t': sb.append("\\t"); break;
      case '\f': sb.append("\\f"); break;
      case '\n': sb.append("\\n"); break;
      default:
	if ((int)s.charAt(i)<32)
	  sb.append("\\"+Integer.toOctalString((int)s.charAt(i)));
	else
	  sb.append(s.charAt(i));
      }
    return sb.toString();
  }
}
