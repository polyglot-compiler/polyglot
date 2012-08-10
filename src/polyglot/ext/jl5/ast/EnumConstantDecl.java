package polyglot.ext.jl5.ast;

import java.util.List;

import polyglot.ast.ClassBody;
import polyglot.ast.ClassMember;
import polyglot.ast.Expr;
import polyglot.ast.Id;
import polyglot.ext.jl5.types.EnumInstance;
import polyglot.types.ConstructorInstance;
import polyglot.types.Flags;
import polyglot.types.ParsedClassType;

/**
 * Decalration of an enum constant.
 */
public interface EnumConstantDecl extends ClassMember, AnnotatedElement {
    /** get args */
    List<Expr> args();

    /** set args */
    EnumConstantDecl args(List<Expr> args);

    /** set name */
    EnumConstantDecl name(Id name);

    /** get name */
    Id name();

    /** set body */
    EnumConstantDecl body(ClassBody body);

    /** get body */
    ClassBody body();

    ParsedClassType type();

    EnumConstantDecl type(ParsedClassType pct);

    ConstructorInstance constructorInstance();

    EnumConstantDecl constructorInstance(ConstructorInstance ci);

    EnumInstance enumInstance();

    EnumConstantDecl enumInstance(EnumInstance ei);

    Flags flags();

    long ordinal();

    EnumConstantDecl ordinal(long ordinal);
}
