
package polyglot.ext.op.runtime;

public class Short extends Integer
{
  public Short( short value)
  {
    super(value);
  }

  public short shortValue()
  {
    return (short) value;
  }
}
