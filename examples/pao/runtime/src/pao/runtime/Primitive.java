package polyglot.ext.op.runtime;

public class Primitive
{
  public static boolean equals(Object o, Object p) {
    return o == p || (o instanceof Primitive && ((Primitive) o).equals(p));
  }
}
