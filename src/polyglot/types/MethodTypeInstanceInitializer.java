package jltools.types;

import java.util.Collections;
import jltools.types.*;

/**
 * A method type instance which is actually an initializer block (either static 
 * or not). We utilize extend MethodTypeInstance since the initilzier block is
 * similar to a method in many ways, but cannot throw any exceptions or 
 * return;
 */
public class MethodTypeInstanceInitializer extends MethodTypeInstance
{
  static final long serialVersionUID = -4797644839050139801L;

  public MethodTypeInstanceInitializer( TypeSystem ts, ClassType ctEnclosing, 
                                        boolean bStatic)
  {
    super ( ts, ctEnclosing, "<INITIALIZER>", null, Collections.EMPTY_LIST, 
            Collections.EMPTY_LIST, 
            AccessFlags.flagsForInt( bStatic ? AccessFlags.STATIC_BIT : 0 ));
  }
}
