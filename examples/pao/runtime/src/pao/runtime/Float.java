
package polyglot.ext.op.runtime;

public class Float extends Double
{
  public Float( float value)
  {
    super(value);
  }

  public float floatValue()
  {
    return (float) value;
  }
}
