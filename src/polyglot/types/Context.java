package jltools.types;

import java.util.List;

/**
 * A context represents a stack of scopes used for looking up types, methods,
 * and variables.
 */
public interface Context extends Resolver
{
    /**
     * A <code>Mark</code> is used to indicate a point in the stack
     * of scopes.
     */
    static interface Mark { }

    /** Return the mark at the top of the stack. */
    Mark mark();

    /** Pop the stack until the top of the stack is <code>mark</code>. */
    void popToMark(Mark m);

    /** Assert that the mark at the top of the stack is <code>mark</code>. */
    void assertMark(Mark m);

    /** The type system. */
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

    /** Get import table currently in scope. */
    ImportTable importTable();

    /** Enter the scope of a source file. */
    void pushSource(ImportTable it);

    /** Leave the scope of a source file. */
    void popSource();

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
