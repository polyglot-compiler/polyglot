/*
 * ClassResolver.java
 */

package jltools.types;

/**
 * ClassResolver
 *
 * Overview:
 *    A ClassResolver is responsible for taking in the name of a class and
 *    returning a ClassType corresponding to that name.  
 * 
 *    Differing concrete implementations of ClassResolver may obey
 *    slightly different contracts in terms of which names they
 *    accept; it is the responsibility of the user to make sure they
 *    have one whose behavior is reasonable.
 **/
public interface ClassResolver {
  // DOCME
  public ClassType findClass(String name) throws TypeCheckException;
  public void findPackage(String name) throws NoClassException;
}
