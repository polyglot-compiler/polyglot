package polyglot.ast;

import polyglot.types.InitializerInstance;
import polyglot.types.Flags;

/**
 * An <code>Initializer</code> is an immutable representation of an
 * initializer block in a Java class (which appears outside of any
 * method).  Such a block is executed before the code for any of the
 * constructors.  Such a block can optionally be static, in which case
 * it is executed when the class is loaded.  
 */
public interface Initializer extends ClassMember 
{
    Flags flags();
    Initializer flags(Flags flags);

    Block body();
    Initializer body(Block body);

    InitializerInstance initializerInstance();
    Initializer initializerInstance(InitializerInstance ii);
}
