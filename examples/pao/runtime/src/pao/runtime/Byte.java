
package polyglot.ext.op.runtime;

public class Byte extends Integer
{
  public Byte( byte value)
  {
    super(value);
  }

  public byte byteValue()
  {
    return (byte) value;
  }
}
