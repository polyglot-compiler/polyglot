package polyglot.ast;

import polyglot.types.Type;
import polyglot.types.Flags;
import polyglot.types.LocalInstance;
import polyglot.types.SemanticException;

/** 
 * A local variable declaration statement: a type, a name and an optional
 * initializer.
 */
public interface LocalDecl extends ForInit
{
    /** Get the type object for the declaration's type. */
    Type declType();

    /** Get the declaration's flags. */
    Flags flags();
    /** Set the declaration's flags. */
    LocalDecl flags(Flags flags);

    /** Get the declaration's type. */
    TypeNode type();
    /** Set the declaration's type. */
    LocalDecl type(TypeNode type);

    /** Get the declaration's name. */
    String name();
    /** Set the declaration's name. */
    LocalDecl name(String name);

    /** Get the declaration's initializer expression, or null. */
    Expr init();
    /** Set the declaration's initializer expression. */
    LocalDecl init(Expr init);

    /**
     * Get the type object for the local we are declaring.  This field may
     * not be valid until after signature disambiguation.
     */
    LocalInstance localInstance();

    /**
     * Set the type object for the local we are declaring.
     */
    LocalDecl localInstance(LocalInstance li);
}
