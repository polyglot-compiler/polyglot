package jltools.types;

import java.util.List;

public interface Context extends Resolver
{
    static interface Mark { }

    Mark mark();
    void popToMark(Mark m);
    void assertMark(Mark m);

    TypeSystem typeSystem();

    /** Add a variable to the current scope. */
    void addVariable(VarInstance vi);

    /** Add a method to the current scope. */
    void addMethod(MethodInstance mi);

    /** Add a type to the current scope. */
    void addType(NamedType t);

    /** Looks up a method in the current scope. */
    MethodInstance findMethod(String name, List argumentTypes)
    	throws SemanticException;

    /** Looks up a local variable or field in the current scope. */
    VarInstance findVariable(String name) throws SemanticException;

    /** Looks up a local variable in the current scope. */
    LocalInstance findLocal(String name) throws SemanticException;

    /** Looks up a field in the current scope. */
    FieldInstance findField(String name) throws SemanticException;

    /**
     * Finds the class which added a field to the scope.
     * This is usually a subclass of <code>findField(name).container()</code>.
     */
    ParsedClassType findFieldScope(String name) throws SemanticException;

    /**
     * Finds the class which added a method to the scope.
     * This is usually a subclass of <code>findMethod(name).container()</code>.
     */
    ParsedClassType findMethodScope(String name) throws SemanticException;

    /** Enter the scope of a class. */
    void pushClass(ParsedClassType c);

    /** Leave the scope of a class. */
    void popClass();

    /** Enter the scope of a method or constructor. */
    void pushCode(CodeInstance f);

    /** Leave the scope of a method or constructor. */
    void popCode();

    /** Enter the scope of a block. */
    void pushBlock();

    /** Leave the scope of a block. */
    void popBlock();
 
    /** Return whether innermost non-block scope is a code scope. */
    boolean inCode();

    /** Returns whether the symbol is defined within the current method. */
    boolean isLocal(String name);

    /** Return the innermost class in scope. */
    ParsedClassType currentClass();
 
    /** Return the innermost method or constructor in scope. */
    CodeInstance currentCode();
}
