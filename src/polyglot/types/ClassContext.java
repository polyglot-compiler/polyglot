
package jltools.types;

import java.util.List;
import java.util.Iterator;

/**
 * Provides a wrapper to abstract the LocalContext and Import Context
 */
public class ClassContext implements Context
{
  ClassContext cParent;
  ClassResolver crResolver;
  JavaClass jc;

  /**
   * Creates a ClassContext which is enclosed within a parent class. It looks for
   * symbols not defined in the JavaClass in cParent
   */
  public ClassContext( JavaClass jc, ClassContext cParent)
  {
    this.jc = jc;
    this.cParent = cParent;
  }

  /**
   * Creates a ClassContext which is not enclosed within any other class; it therefore
   * looks for symbols not defined in the JavaClass in the Resolver
   */
  public ClassContext ( JavaClass jc, ClassResolver crResolver)
  {
    this.jc = jc;
    this.crResolver = crResolver;
  }

  /**
   * Finds the type of a particular string within a context. If it is not found
   * in this context, checks the parent context, and then the superclass
   */
  public TypeInstance lookup(String s) throws TypeCheckError
  {
    // FIXME: perhaps we should load the fields and methods into a hashtable.
    
    TypeInstance tiSuperClassLookup = null, tiEnclosingClassLookup = null;
    List l = jc.getFields();
    for (Iterator i = l.listIterator() ; i.hasNext(); )
    {
      FieldInstance fi = (FieldInstance)i.next();
      if (fi.getName() != null && fi.getName().equals(s)) 
        return fi;
    }
    
    l = jc.getMethods();
    for ( Iterator i = l.listIterator() ; i.hasNext() ; )
    {
      MethodTypeInstance mti = (MethodTypeInstance)i.next();
      if (mti.getName().equals ( s ) ) 
      {
        return mti;
      }
    }
    
    // FIXME: Need to check all superclasses for the lookup here, make sure that 
    //        it isn't in both a superclass as well as an enclosing class.


    // check for name in enclosing class.
    if (cParent != null)
    {
      tiEnclosingClassLookup = cParent.lookup(s);
      if (tiSuperClassLookup != null && tiEnclosingClassLookup != null)
      {
        throw new TypeCheckError("Ambigious reference to \"" + s + "\"; found in both an enclosing class " +
                                 "and superclass");
      }
      if (tiSuperClassLookup != null) 
        return tiSuperClassLookup;
    }
    return tiSuperClassLookup;
  }

  /**
   * Returns whether the particular symbol is defined within the current method scope. 
   * If it isn't in this scope, we ask the parent scope, as long as it is not 
   * a ClassContext
   */
  public boolean isDefinedLocally(String s)
  {
    return false;
  }
}
