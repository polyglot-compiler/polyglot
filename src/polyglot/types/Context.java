/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2012 Polyglot project group, Cornell University
 * Copyright (c) 2006-2012 IBM Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program and the accompanying materials are made available under
 * the terms of the Lesser GNU Public License v2.0 which accompanies this
 * distribution.
 *
 * The development of the Polyglot project has been supported by a
 * number of funding sources, including DARPA Contract F30602-99-1-0533,
 * monitored by USAF Rome Laboratory, ONR Grants N00014-01-1-0968 and
 * N00014-09-1-0652, NSF Grants CNS-0208642, CNS-0430161, CCF-0133302,
 * and CCF-1054172, AFRL Contract FA8650-10-C-7022, an Alfred P. Sloan
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.types;

import java.util.List;

import polyglot.ast.Lang;
import polyglot.util.Copy;

/**
 * A context represents a stack of scopes used for looking up types, methods,
 * and variables.  To push a new scope call one of the {@code push*}
 * methods to return a new context.  The old context may still be used
 * and may be accessed directly through a call to {@code pop()}.
 * While the stack of scopes is treated functionally, each individual
 * scope is updated in place.  Names added to the context are added
 * in the current scope.
 */
public interface Context extends Resolver, Copy<Context> {
    /** The language this context represents. */
    Lang lang();

    /** The type system. */
    TypeSystem typeSystem();

    /** Add a variable to the current scope. */
    void addVariable(VarInstance vi);

    /** Add a method to the current scope. */
    void addMethod(MethodInstance mi);

    /** Add a named type object to the current scope. */
    void addNamed(Named t);

    /** Looks up a method in the current scope.
     * @param formalTypes A list of {@code Type}.
     * @see polyglot.types.Type
     */
    MethodInstance findMethod(String name, List<? extends Type> formalTypes)
            throws SemanticException;

    /** Looks up a local variable or field in the current scope. */
    VarInstance findVariable(String name) throws SemanticException;

    /** Looks up a local variable or field in the current scope. */
    VarInstance findVariableSilent(String name);

    /** Looks up a local variable in the current scope. */
    LocalInstance findLocal(String name) throws SemanticException;

    /** Looks up a local variable in the current scope. */
    LocalInstance findLocalSilent(String name);

    /** Looks up a field in the current scope. */
    FieldInstance findField(String name) throws SemanticException;

    /**
     * Finds the class which added a field to the scope.
     * This is usually a subclass of {@code findField(name).container()}.
     */
    ClassType findFieldScope(String name) throws SemanticException;

    /**
     * Finds the class which added a method to the scope.
     * This is usually a subclass of {@code findMethod(name).container()}.
     */
    ClassType findMethodScope(String name) throws SemanticException;

    /** Looks up a label in the current scope. */
    String findLabelSilent(String label);

    /** Get import table currently in scope. */
    ImportTable importTable();

    /** Get the outer-most resolver for the source file currently in scope.
     * This is usually just the system resolver.
     * @deprecated
     */
    @Deprecated
    Resolver outerResolver();

    /** Enter the scope of a source file. */
    Context pushSource(ImportTable it);

    /** Enter the scope of a class. */
    Context pushClass(ParsedClassType scope, ClassType type);

    /** Enter the scope of a method or constructor. */
    Context pushCode(CodeInstance f);

    /** Enter the scope of a block. */
    Context pushBlock();

    /** Enter the scope of a label. */
    Context pushLabel(String label);

    /** Enter a static scope. In general, this is only used for
     * explicit constructor calls; static methods, initializers of static
     * fields and static initializers are generally handled by pushCode().
     */
    Context pushStatic();

    /** Pop the context. */
    Context pop();

    /** Return whether innermost non-block scope is a code scope. */
    boolean inCode();

    /** Returns whether the symbol is defined within the current method. */
    boolean isLocal(String name);

    /** Return the code instance that defines the local symbol with the given name, or null. */
    CodeInstance definingCodeDef(String name);

    /**
     * Returns whether the current context is a static context.
     * A statement of expression occurs in a static context if and only if the
     * inner-most method, constructor, instance initializer, static initializer,
     * field initializer, or explicit constructor statement enclosing the
     * statement or expressions is a static method, static initializer, the
     * variable initializer of a static variable, or an explicity constructor
     * invocation statment. (Java Language Spec, 2nd Edition, 8.1.2)
     */
    boolean inStaticContext();

    /** Return the innermost class in scope. */
    ClassType currentClass();

    /** Return the innermost class in scope. */
    ParsedClassType currentClassScope();

    /** Return the innermost method or constructor in scope. */
    CodeInstance currentCode();

    /** The current package, or null if not in a package. */
    Package package_();
}
