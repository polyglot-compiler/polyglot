/*
 * AccessFlags.java
 */

package jltools.types;

/**
 * AccessFlags
 *
 * Overview:
 *    A mutable set of access flags.
 **/
public class AccessFlags implements Cloneable {

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

  public void setPublic(boolean val) {
    if (val)
      bits |= PUBLIC_BIT;
    else
      bits &= ~PUBLIC_BIT;
  }

  public boolean isPublic() {
    return (bits & PUBLIC_BIT) != 0;
  }

  public void setPrivate(boolean val) {
    if (val)
      bits |= PRIVATE_BIT;
    else
      bits &= ~PRIVATE_BIT;
  }

  public boolean isPrivate() {
    return (bits & PRIVATE_BIT) != 0;
  }

  public void setProtected(boolean val) {
    if (val)
      bits |= PROTECTED_BIT;
    else
      bits &= ~PROTECTED_BIT;
  }

  public boolean isProtected() {
    return (bits & PROTECTED_BIT) != 0;
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

  private static int PUBLIC_BIT       = 1;
  private static int PRIVATE_BIT      = 2;
  private static int PROTECTED_BIT    = 4;
  private static int STATIC_BIT       = 8;
  private static int FINAL_BIT        = 16;
  private static int SYNCHRONIZED_BIT = 32;
  private static int TRANSIENT_BIT    = 64;
  private static int NATIVE_BIT       = 128;
  private static int INTERFACE_BIT    = 256;
  private static int ABSTRACT_BIT     = 512;
  private static int VOLATILE_BIT     = 1024;
  
  // Currently, the above bits fit into a short.  We provide an int here
  // for subclasses.
  protected int bits;
}
