package polyglot.ext.pao.runtime;

/**
 * Boxed primitves.
 */
public class Primitive
{
  /** Method used to implement <code>o == p</code> when <code>o</code> or
   * <code>p</code> could be a boxed primitive.  Boxed primitives are compared
   * by their primitive value, not by identity.
   */
  public static boolean equals(Object o, Object p) {
    return o == p || (o instanceof Primitive && ((Primitive) o).equals(p));
  }
}
