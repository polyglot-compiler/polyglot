package jltools.util;


public class ErrorInfo
{
  public static final int WARNING            = -1;
  public static final int INTERNAL_ERROR     = 0;
  public static final int IO_ERROR           = 1;
  public static final int LEXICAL_ERROR      = 2;
  public static final int SYNTAX_ERROR       = 3;
  public static final int SEMANTIC_ERROR     = 4;

  protected int type;
  protected String message;
  protected int lineNumber;
  
  public ErrorInfo(int type, String message, int lineNumber)
  {
    this.type = type;
    this.message = message;
    this.lineNumber = lineNumber;
  }

  public int getErrorType()
  {
    return type;
  }

  public String getMessage()
  {
    return message;
  }

  public int getLineNumber()
  {
    return lineNumber;
  }
}

