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

/**
 * A {@code MethodInstance} represents the type information for a Java
 * method.
 */
public interface MethodInstance extends FunctionInstance, MemberInstance,
        Declaration {
    /**
     * The method's name.
     */
    String name();

    /**
     * Destructively set the method's name.
     * @param name the name to set
     */
    void setName(String name);

    /** Non-destructive updates. */
    MethodInstance flags(Flags flags);

    MethodInstance name(String name);

    MethodInstance returnType(Type returnType);

    MethodInstance formalTypes(List<? extends Type> l);

    MethodInstance throwTypes(List<? extends Type> l);

    MethodInstance container(ReferenceType container);

    MethodInstance orig();

    /**
     * Get the list of methods this method (potentially) overrides, in order
     * from this class (i.e., including {@code this}) to super classes.
     * @return A list of {@code MethodInstance}, starting with
     * {@code this}. Note that this list does not include methods declared
     * in interfaces. Use {@code implemented} for that.
     * @see polyglot.types.MethodInstance
     */
    List<MethodInstance> overrides();

    /**
     * Return true if this method can override {@code mi}, false otherwise.
     */
    boolean canOverride(MethodInstance mi);

    /**
     * Return true if this method can override {@code mi}, throws
     * a SemanticException otherwise.
     */
    void checkOverride(MethodInstance mi) throws SemanticException;

    /**
     * Get the set of methods this method implements.  No ordering is
     * specified since the superinterfaces need not form a linear list
     * (i.e., they can form a tree).  
     * @return List[MethodInstance]
     */
    List<? extends MethodInstance> implemented();

    /**
     * Return true if this method has the same signature as {@code mi}.
     */
    boolean isSameMethod(MethodInstance mi);

    /**
     * Return true if this method can be called with name {@code name}
     * and actual parameters of types {@code actualTypes}.
     * @param name The method to call.
     * @param actualTypes A list of argument types of type {@code Type}.
     * @see polyglot.types.Type
     */
    boolean methodCallValid(String name, List<? extends Type> actualTypes);

    /**
     * Get the list of methods this method (potentially) overrides, in order
     * from this class (i.e., including {@code this}) to super classes.
     * This method should not be called except by {@code TypeSystem}
     * and by subclasses.
     * @return A list of {@code MethodInstance}, starting with
     * {@code this}.
     * @see polyglot.types.MethodInstance
     */
    List<MethodInstance> overridesImpl();

    /**
     * Return true if this method can override {@code mi}.
     * This method should not be called except by {@code TypeSystem}
     * and by subclasses.
     * If quiet is true and this method cannot override {@code mi}, then
     * false is returned; otherwise, if quiet is false and this method cannot 
     * override {@code mi}, then a SemanticException is thrown.
     * @param quiet If true, then no Semantic Exceptions will be thrown, and the
     *              return value will be true or false. Otherwise, if the method
     *              cannot override, then a SemanticException will be thrown, else
     *              the method will return true.
     */
    boolean canOverrideImpl(MethodInstance mi, boolean quiet)
            throws SemanticException;

    /**
     * Get the set of methods in rt and its superinterfaces that
     * this method implements.  No ordering is specified.
     * @return List[MethodInstance]
     * @param rt The point in the type hierarchy to begin looking for methods.
     */
    List<MethodInstance> implementedImpl(ReferenceType rt);

    /**
     * Return true if this method has the same signature as {@code mi}.
     * This method should not be called except by {@code TypeSystem}
     * and by subclasses.
     */
    boolean isSameMethodImpl(MethodInstance mi);

    /**
     * Return true if this method can be called with name {@code name}
     * and actual parameters of types {@code actualTypes}.
     * This method should not be called except by {@code TypeSystem}
     * and by subclasses.
     */
    boolean methodCallValidImpl(String name, List<? extends Type> actualTypes);
}
