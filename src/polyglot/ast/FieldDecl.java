package jltools.ast;

import jltools.types.Type;
import jltools.types.Flags;
import jltools.types.FieldInstance;
import jltools.types.SemanticException;

/**
 * A <code>FieldDecl</code> is an immutable representation of the declaration 
 * of a field of a class.
 */
public interface FieldDecl extends ClassMember
{
    Type declType();

    Flags flags();
    FieldDecl flags(Flags flags);

    TypeNode type();
    FieldDecl type(TypeNode type);

    String name();
    FieldDecl name(String name);

    Expr init();
    FieldDecl init(Expr init);

    FieldInstance fieldInstance();
    FieldDecl fieldInstance(FieldInstance fi);
}
