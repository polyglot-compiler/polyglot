package jltools.util;


public class ErrorInfo
{
  public static final int WARNING            = 0;
  public static final int INTERNAL_ERROR     = 1;
  public static final int IO_ERROR           = 2;
  public static final int LEXICAL_ERROR      = 3;
  public static final int SYNTAX_ERROR       = 4;
  public static final int SEMANTIC_ERROR     = 5;

  protected int kind;
  protected String message;
  protected int lineNumber;
  
  public ErrorInfo(int kind, String message, int lineNumber)
  {
    this.kind = kind;
    this.message = message;
    this.lineNumber = lineNumber;
  }

  public int getErrorKind()
  {
    return kind;
  }

  public String getMessage()
  {
    return message;
  }

  public int getLineNumber()
  {
    return lineNumber;
  }

  public String getErrorString()
  {
    return getErrorString( kind);
  }

  public static String getErrorString( int kind)
  {
    switch( kind) {
    case WARNING:
      return "Warning";
    case INTERNAL_ERROR:
      return "Interal Error";
    case IO_ERROR:
      return "I/O Error";
    case LEXICAL_ERROR:
      return "Lexical Error";
    case SYNTAX_ERROR:
      return "Syntax Error";
    case SEMANTIC_ERROR:
      return "Semantic Error";
    default:
      return "(Unknown)";
    }
  }
}

