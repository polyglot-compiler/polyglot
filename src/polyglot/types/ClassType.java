/*
 * JavaClassType.java
 */

package jltools.types;

/**
 * JavaClassType
 *
 * Overview:
 *    A JavaClassType is a type for an ordinary Java type.
 *
 *    ==> _All_ types are immutable.
 **/
public class ClassType extends Type {
  // Creates a new type in the given typeSystem.
  public ClassType(TypeSystem ts, String name, boolean canonical) {
    super(ts);
    this.name = name;
    this.canonical = canonical;
  }

  public String getName() {
    return name;
  }
  public String getShortName() {
    return TypeSystem.getShortNameComponent(name);
  }

  private String name;
  private boolean canonical;

  public boolean isPrimitive() { return false; }
  public boolean isCanonical() { return canonical; }
  public boolean equals(Object o) {
    if (! (o instanceof ClassType)) return false;

    ClassType t = (ClassType) o;
    return t.name.equals(name) && t.canonical == canonical ;
  }
  public int hashCode() {
    return name.hashCode();
  }
}


