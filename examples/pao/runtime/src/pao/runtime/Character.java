package polyglot.ext.pao.runtime;

public class Character extends Integer
{
  public Character( char value)
  {
    super(value);
  }

  public char charValue()
  {
    return (char) value;
  }
}
