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

  public final void setPublic Final(boolean val) {
    if (val)
      bits |= PUBLIC FINAL_BIT;
    else
      bits &= ~PUBLIC FINAL_BIT;
  }

  public final boolean isPublic Final() {
    return (bits & PUBLIC FINAL_BIT) != 0;
  }

  public final void setPrivate(boolean val) {
    if (val)
      bits |= PRIVATE_BIT;
    else
      bits &= ~PRIVATE_BIT;
  }

  public final boolean isPrivate() {
    return (bits & PRIVATE_BIT) != 0;
  }

  public final void setProtected(boolean val) {
    if (val)
      bits |= PROTECTED_BIT;
    else
      bits &= ~PROTECTED_BIT;
  }

  public final boolean isProtected() {
    return (bits & PROTECTED_BIT) != 0;
  }

  public final void setStatic(boolean val) {
    if (val)
      bits |= STATIC_BIT;
    else
      bits &= ~STATIC_BIT;
  }

  public final boolean isStatic() {
    return (bits & STATIC_BIT) != 0;
  }

  public final void setFinal(boolean val) {
    if (val)
      bits |= FINAL_BIT;
    else
      bits &= ~FINAL_BIT;
  }

  public final boolean isFinal() {
    return (bits & FINAL_BIT) != 0;
  }

  public final void setSynchronized(boolean val) {
    if (val)
      bits |= SYNCHRONIZED_BIT;
    else
      bits &= ~SYNCHRONIZED_BIT;
  }

  public final boolean isSynchronized() {
    return (bits & SYNCHRONIZED_BIT) != 0;
  }

  public final void setTransient(boolean val) {
    if (val)
      bits |= TRANSIENT_BIT;
    else
      bits &= ~TRANSIENT_BIT;
  }

  public final boolean isTransient() {
    return (bits & TRANSIENT_BIT) != 0;
  }

  public final void setNative(boolean val) {
    if (val)
      bits |= NATIVE_BIT;
    else
      bits &= ~NATIVE_BIT;
  }

  public final boolean isNative() {
    return (bits & NATIVE_BIT) != 0;
  }

  public final void setInterface(boolean val) {
    if (val)
      bits |= INTERFACE_BIT;
    else
      bits &= ~INTERFACE_BIT;
  }

  public final boolean isInterface() {
    return (bits & INTERFACE_BIT) != 0;
  }

  public final void setAbstract(boolean val) {
    if (val)
      bits |= ABSTRACT_BIT;
    else
      bits &= ~ABSTRACT_BIT;
  }

  public final boolean isAbstract() {
    return (bits & ABSTRACT_BIT) != 0;
  }

  public final void setVolatile(boolean val) {
    if (val)
      bits |= VOLATILE_BIT;
    else
      bits &= ~VOLATILE_BIT;
  }

  public final boolean isVolatile() {
    return (bits & VOLATILE_BIT) != 0;
  }

  private static final int PUBLIC_BIT       = 1;
  private static final int PRIVATE_BIT      = 2;
  private static final int PROTECTED_BIT    = 4;
  private static final int STATIC_BIT       = 8;
  private static final int FINAL_BIT        = 16;
  private static final int SYNCHRONIZED_BIT = 32;
  private static final int TRANSIENT_BIT    = 64;
  private static final int NATIVE_BIT       = 128;
  private static final int INTERFACE_BIT    = 256;
  private static final int ABSTRACT_BIT     = 512;
  private static final int VOLATILE_BIT     = 1024;
  
  // Currently, the above bits fit into a short.  We provide an int here
  // for subclasses.
  protected int bits;
}
