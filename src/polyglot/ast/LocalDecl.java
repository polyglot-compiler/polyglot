package jltools.ast;

import jltools.types.Type;
import jltools.types.Flags;
import jltools.types.LocalInstance;
import jltools.types.SemanticException;

/** 
 * A variable declaration statement: a type, a name and an optional
 * initializer.
 */
public interface LocalDecl extends ForInit
{
    Type declType();

    Flags flags();
    LocalDecl flags(Flags flags);

    TypeNode type();
    LocalDecl type(TypeNode type);

    String name();
    LocalDecl name(String name);

    Expr init();
    LocalDecl init(Expr init);

    LocalInstance localInstance();
    LocalDecl localInstance(LocalInstance li);
}
