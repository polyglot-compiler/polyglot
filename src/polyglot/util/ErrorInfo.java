package jltools.util;


public class ErrorInfo
{
  public static final int LEXICAL_ERROR      = 1;
  public static final int SYNTAX_ERROR       = 2;
  public static final int SEMANTIC_ERROR     = 3;

  protected int type;
  protected String message;
  
  public ErrorInfo(int type, String message)
  {
    this.type = type;
    this.message = message;
  }

  public int getErrorType()
  {
    return type;
  }

  public String getMessage()
  {
    return message;
  }
}
