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

/**
 * A LazyClassInitializer is responsible for initializing members of
 * a class after it has been created.  Members are initialized lazily
 * to correctly handle cyclic dependencies between classes.
 */
public interface LazyClassInitializer extends LazyInitializer {
    /**
     * Return true if the class is from a class file.
     */
    public boolean fromClassFile();

    /** Set the class type we're initializing. */
    public void setClass(ParsedClassType ct);

    /**
     * Initialize {@code ct}'s superclass.
     * This method ensures the superclass of the class is initialized to a
     * canonical type, or throws a {@code MissingDependencyException}.
     */
    public void initSuperclass();

    /**
     * Initialize {@code ct}'s constructors.
     * This method ensures the list of constructors is populated with
     * canonical ConstructorInstances, or throws a {@code MissingDependencyException}.
     */
    public void canonicalConstructors();

    /**
     * Initialize {@code ct}'s methods.
     * This method ensures the list of methods is populated with
     * canonical MethodInstances, or throws a {@code MissingDependencyException}.
     */
    public void canonicalMethods();

    /**
     * Initialize {@code ct}'s fields.
     * This method ensures the list of fields is populated with
     * canonical FieldInstances, or throws a {@code MissingDependencyException}.
     */
    public void canonicalFields();

    /**
     * Initialize {@code ct}'s constructors.
     * This method ensures the list of fields is populated with (possibly
     * non-canonical) ConstructorInstances, or throws a {@code MissingDependencyException}.
     */
    public void initConstructors();

    /**
     * Initialize {@code ct}'s methods.
     * This method ensures the list of fields is populated with (possibly
     * non-canonical) MethodInstances, or throws a {@code MissingDependencyException}.
     */
    public void initMethods();

    /**
     * Initialize {@code ct}'s fields.
     * This method ensures the list of fields is populated with (possibly
     * non-canonical) FieldInstances, or throws a {@code MissingDependencyException}.
     */
    public void initFields();

    /**
     * Initialize {@code ct}'s member classes.
     * This method ensures the member classes of the class are initialized to
     * canonical types, or throws a {@code MissingDependencyException}.
     */
    public void initMemberClasses();

    /**
     * Initialize {@code ct}'s interfaces.
     * This method ensures the interfaces of the class are initialized to
     * canonical types, or throws a {@code MissingDependencyException}.
     */
    public void initInterfaces();
}
