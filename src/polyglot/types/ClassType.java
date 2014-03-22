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

import polyglot.util.Enum;
import polyglot.util.SerialVersionUID;

/**
 * A {@code ClassType} represents a class, either loaded from a
 * classpath, parsed from a source file, or obtained from other source.
 * A {@code ClassType} is not necessarily named.
 */
public interface ClassType extends Importable, ReferenceType, MemberInstance,
        Declaration {
    public static class Kind extends Enum {
        private static final long serialVersionUID =
                SerialVersionUID.generate();

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

    /** Get the class's kind: top-level, member, local, or anonymous. */
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
     * A list of {@code ConstructorInstance}.
     * @see polyglot.types.ConstructorInstance
     */
    List<? extends ConstructorInstance> constructors();

    /**
     * The class's member classes.
     * A list of {@code ClassType}.
     * @see polyglot.types.ClassType
     */
    List<? extends ClassType> memberClasses();

    /** Returns the member class with the given name, or null. */
    ClassType memberClassNamed(String name);

    /** Get a field by name, or null. */
    @Override
    FieldInstance fieldNamed(String name);

    /** Return true if the class is strictly contained in {@code outer}. */
    boolean isEnclosed(ClassType outer);

    /**
     * Implementation of {@code isEnclosed}.
     * This method should only be called by the {@code TypeSystem}
     * or by a subclass.
     */
    boolean isEnclosedImpl(ClassType outer);

    /** Return true if an object of the class has
     * an enclosing instance of {@code encl}. */
    boolean hasEnclosingInstance(ClassType encl);

    /**
     * Implementation of {@code hasEnclosingInstance}.
     * This method should only be called by the {@code TypeSystem}
     * or by a subclass.
     */
    boolean hasEnclosingInstanceImpl(ClassType encl);

    /** The class's outer class, or null if a top-level class. */
    ClassType outer();
}
