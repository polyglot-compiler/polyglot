/*
 * AccessFlags.java
 */

package jltools.types;

import java.lang.reflect.Modifier;

/**
 * AccessFlags
 *
 * Overview:
 *    A mutable set of access flags.
 *    We represent pacakge protection as the abscence of private, public
 *    and protected.
 **/
public class AccessFlags implements Cloneable, java.io.Serializable {

  static final long serialVersionUID = -5861922980904884299L;

  /**
   * Effects: returns a new accessflags object with no accessflags set.
   **/
  public AccessFlags() {
    // bits defaults to 0.
  }

  /**
   * Returns a copy of this.
   **/
  public AccessFlags copy() {
    AccessFlags other = new AccessFlags();
    other.bits = bits;
    return other;
  }

  public void merge(AccessFlags other) {
    bits = bits | other.bits;
  }

  /**
   * Given the JVM encoding of a set of flags, returns the AccessFlags object
   * for that encoding.
   **/
  public static AccessFlags flagsForInt(int mod) {
    AccessFlags flags = new AccessFlags();
    /*    if ((fl & PUBLIC_BIT) != 0)   { flags.setPublic(true); }
          if ((fl & PRIVATE_BIT) != 0)   { flags.setPrivate(true); }
          if ((fl & PROTECTED_BIT) != 0)   { flags.setProtected(true); }
          if ((fl & STATIC_BIT) != 0)   { flags.setStatic(true); }
          if ((fl & FINAL_BIT) != 0)  { flags.setFinal(true); }
          if ((fl & SYNCHRONIZED_BIT) != 0)  { flags.setSynchronized(true); }
          if ((fl & VOLATILE_BIT) != 0)  { flags.setVolatile(true); }
          if ((fl & TRANSIENT_BIT) != 0)  { flags.setTransient(true); }
          if ((fl & NATIVE_BIT) != 0) { flags.setNative(true); }
          if ((fl & INTERFACE_BIT) != 0) { flags.setInterface(true); }
          if ((fl & ABSTRACT_BIT) != 0) { flags.setAbstract(true); }
          if ((fl & STRICTFP_BIT) != 0) { flags.setStrictFloatingPoint(true); }
    */
    flags.setPublic( Modifier.isPublic( mod ));
    flags.setPrivate( Modifier.isPrivate (mod));
    flags.setProtected ( Modifier.isProtected (mod ));
    flags.setStatic ( Modifier.isStatic(mod));
    flags.setFinal( Modifier.isFinal(mod));
    flags.setSynchronized ( Modifier.isSynchronized (mod));
    flags.setVolatile ( Modifier.isVolatile(mod));
    flags.setTransient ( Modifier.isTransient(mod));
    flags.setNative ( Modifier.isNative(mod));
    flags.setInterface ( Modifier.isInterface(mod));
    flags.setAbstract ( Modifier.isAbstract ( mod ));
    flags.setStrictFloatingPoint ( Modifier.isStrict(mod));
                     
    return flags;
  }

  public void setPublic(boolean val) {
    if (val)
    {
      bits |= PUBLIC_BIT;
      bits &= ~PROTECTED_BIT;
      bits &= ~PRIVATE_BIT;
    }
    else
      bits &= ~PUBLIC_BIT;
  }

  public boolean isPublic() {
    return (bits & PUBLIC_BIT) != 0;
  }

  public void setPrivate(boolean val) {
    if (val)
    {
      bits |= PRIVATE_BIT;
      bits &= ~PUBLIC_BIT;
      bits &= ~PROTECTED_BIT;
    }
    else
      bits &= ~PRIVATE_BIT;
  }

  public boolean isPrivate() {
    return (bits & PRIVATE_BIT) != 0;
  }

  public void setProtected(boolean val) {
    if (val)
    {
      bits |= PROTECTED_BIT;
      bits &= ~PRIVATE_BIT;
      bits &= ~PUBLIC_BIT;
    }
    else
      bits &= ~PROTECTED_BIT;
  }

  public boolean isProtected() {
    return (bits & PROTECTED_BIT) != 0;
  }

  public void  setPackage()
  {
    bits &= ~ ( PUBLIC_BIT | PROTECTED_BIT | PRIVATE_BIT) ;
  }

  public boolean isPackage()
  {
    return ((bits & ( PUBLIC_BIT | PROTECTED_BIT | PRIVATE_BIT))  == 0);
  }

  public void setStatic(boolean val) {
    if (val)
      bits |= STATIC_BIT;
    else
      bits &= ~STATIC_BIT;
  }

  public boolean isStatic() {
    return (bits & STATIC_BIT) != 0;
  }

  public void setFinal(boolean val) {
    if (val)
      bits |= FINAL_BIT;
    else
      bits &= ~FINAL_BIT;
  }

  public boolean isFinal() {
    return (bits & FINAL_BIT) != 0;
  }

  public void setSynchronized(boolean val) {
    if (val)
      bits |= SYNCHRONIZED_BIT;
    else
      bits &= ~SYNCHRONIZED_BIT;
  }

  public boolean isSynchronized() {
    return (bits & SYNCHRONIZED_BIT) != 0;
  }

  public void setTransient(boolean val) {
    if (val)
      bits |= TRANSIENT_BIT;
    else
      bits &= ~TRANSIENT_BIT;
  }

  public boolean isTransient() {
    return (bits & TRANSIENT_BIT) != 0;
  }

  public void setNative(boolean val) {
    if (val)
      bits |= NATIVE_BIT;
    else
      bits &= ~NATIVE_BIT;
  }

  public boolean isNative() {
    return (bits & NATIVE_BIT) != 0;
  }

  public void setInterface(boolean val) {
    if (val)
      bits |= INTERFACE_BIT;
    else
      bits &= ~INTERFACE_BIT;
  }

  public boolean isInterface() {
    return (bits & INTERFACE_BIT) != 0;
  }

  public void setAbstract(boolean val) {
    if (val)
      bits |= ABSTRACT_BIT;
    else
      bits &= ~ABSTRACT_BIT;
  }

  public boolean isAbstract() {
    return (bits & ABSTRACT_BIT) != 0;
  }

  public void setVolatile(boolean val) {
    if (val)
      bits |= VOLATILE_BIT;
    else
      bits &= ~VOLATILE_BIT;
  }

  public boolean isVolatile() {
    return (bits & VOLATILE_BIT) != 0;
  }

  public void setStrictFloatingPoint(boolean val) {
      if (val)
        bits |= STRICTFP_BIT;
      else
        bits &= ~STRICTFP_BIT;
    }

    public boolean isStrictFloatingPoint() {
      return (bits & STRICTFP_BIT) != 0;
  }

  public String getStringRepresentation()
  {
     String s = "";
     s += ((bits & PUBLIC_BIT) != 0    ? "public " : "");
     s += ((bits & PRIVATE_BIT) != 0   ? "private " : "");
     s += ((bits & PROTECTED_BIT) != 0 ? "protected " : "");
     s += ((bits & STATIC_BIT)    != 0 ? "static " : "");
     s += ((bits & FINAL_BIT)     != 0 ? "final " : "");
     s += ((bits & SYNCHRONIZED_BIT)  != 0 ? "synchronized " : "");
     s += ((bits & TRANSIENT_BIT)     != 0 ? "transient " : "");
     s += ((bits & NATIVE_BIT)        != 0 ? "native " : "");
     s += ((bits & INTERFACE_BIT)     != 0 ? "interface " : "");
     s += ((bits & ABSTRACT_BIT)      != 0 ? "abstract " : "");
     s += ((bits & VOLATILE_BIT)      != 0 ? "volatile " : "");
     s += ((bits & STRICTFP_BIT)      != 0 ? "strictfp " : "");
     return s;
  }

  public static final int PUBLIC_BIT       = Modifier.PUBLIC;
  public static final int PROTECTED_BIT    = Modifier.PROTECTED;
  public static final int PRIVATE_BIT      = Modifier.PRIVATE;
 
  public static final int STATIC_BIT       = Modifier.STATIC;
  public static final int FINAL_BIT        = Modifier.FINAL;
  public static final int SYNCHRONIZED_BIT = Modifier.SYNCHRONIZED;
  public static final int TRANSIENT_BIT    = Modifier.TRANSIENT;
  public static final int NATIVE_BIT       = Modifier.NATIVE;
  public static final int INTERFACE_BIT    = Modifier.INTERFACE;
  public static final int ABSTRACT_BIT     = Modifier.ABSTRACT;
  public static final int VOLATILE_BIT     = Modifier.VOLATILE;
  public static final int STRICTFP_BIT     = Modifier.STRICT;

  public boolean equals( Object o)
  {
    if( o instanceof AccessFlags) {
      return bits == ((AccessFlags)o).bits;
    }
    else {
      return false;
    }
  }
  
  // Currently, the above bits fit into a short.  We provide an int here
  // for subclasses.
  protected int bits;
}
