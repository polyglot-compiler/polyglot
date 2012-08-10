/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2008 Polyglot project group, Cornell University
 * Copyright (c) 2006-2008 IBM Corporation
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
 * monitored by USAF Rome Laboratory, ONR Grant N00014-01-1-0968, NSF
 * Grants CNS-0208642, CNS-0430161, and CCF-0133302, an Alfred P. Sloan
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyglot.types;

import java.util.List;

import polyglot.util.Enum;

/**
 * A <code>ClassType</code> represents a class, either loaded from a
 * classpath, parsed from a source file, or obtained from other source.
 * A <code>ClassType</code> is not necessarily named.
 */
public interface ClassType extends Importable, ReferenceType, MemberInstance,
        Declaration {
    public static class Kind extends Enum {
        public Kind(String name) {
            super(name);
        }
    }

    public static final Kind TOP_LEVEL = new Kind("top-level");
    public static final Kind MEMBER = new Kind("member");
    public static final Kind LOCAL = new Kind("local");
    public static final Kind ANONYMOUS = new Kind("anonymous");

    /**
     * A resolver to access member classes of the class.
     */
    Resolver resolver();

    /** Get the class's kind. */
    Kind kind();

    /**
     * Return true if the class is top-level (i.e., not inner).
     * Equivalent to kind() == TOP_LEVEL.
     */
    boolean isTopLevel();

    /**
     * Return true if the class is an inner class.
     * Equivalent to kind() == MEMBER || kind() == LOCAL || kind() == ANONYMOUS.
     * @deprecated Was incorrectly defined. Use isNested for nested classes, 
     *          and isInnerClass for inner classes.
     */
    @Deprecated
    boolean isInner();

    /**
     * Return true if the class is a nested.
     * Equivalent to kind() == MEMBER || kind() == LOCAL || kind() == ANONYMOUS.
     */
    boolean isNested();

    /**
     * Return true if the class is an inner class, that is, it is a nested
     * class that is not explicitly or implicitly declared static; an interface
     * is never an inner class.
     */
    boolean isInnerClass();

    /**
     * Return true if the class is a member class.
     * Equivalent to kind() == MEMBER.
     */
    boolean isMember();

    /**
     * Return true if the class is a local class.
     * Equivalent to kind() == LOCAL.
     */
    boolean isLocal();

    /**
     * Return true if the class is an anonymous class.
     * Equivalent to kind() == ANONYMOUS.
     */
    boolean isAnonymous();

    /**
     * Return true if the class declaration occurs in a static context.
     * Is used to determine if a nested class is implicitly static.
     */
    boolean inStaticContext();

    /**
     * The class's constructors.
     * A list of <code>ConstructorInstance</code>.
     * @see polyglot.types.ConstructorInstance
     */
    List<? extends ConstructorInstance> constructors();

    /**
     * The class's member classes.
     * A list of <code>ClassType</code>.
     * @see polyglot.types.ClassType
     */
    List<? extends ClassType> memberClasses();

    /** Returns the member class with the given name, or null. */
    ClassType memberClassNamed(String name);

    /** Get a field by name, or null. */
    @Override
    FieldInstance fieldNamed(String name);

    /** Return true if the class is strictly contained in <code>outer</code>. */
    boolean isEnclosed(ClassType outer);

    /**
     * Implementation of <code>isEnclosed</code>.
     * This method should only be called by the <code>TypeSystem</code>
     * or by a subclass.
     */
    boolean isEnclosedImpl(ClassType outer);

    /** Return true if an object of the class has
     * an enclosing instance of <code>encl</code>. */
    boolean hasEnclosingInstance(ClassType encl);

    /**
     * Implementation of <code>hasEnclosingInstance</code>.
     * This method should only be called by the <code>TypeSystem</code>
     * or by a subclass.
     */
    boolean hasEnclosingInstanceImpl(ClassType encl);

    /** The class's outer class if this is a nested class, or null. */
    ClassType outer();
}
