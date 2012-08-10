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
     * Initialize <code>ct</code>'s superclass.
     * This method ensures the superclass of the class is initailized to a
     * canonical type, or throws a <code>MissingDependencyException</code>.
     */
    public void initSuperclass();

    /**
     * Initialize <code>ct</code>'s constructors.
     * This method ensures the list of constructors is populated with
     * canonical ConstructorInstances, or throws a <code>MissingDependencyException</code>.
     */
    public void canonicalConstructors();

    /**
     * Initialize <code>ct</code>'s methods.
     * This method ensures the list of methods is populated with
     * canonical MethodInstances, or throws a <code>MissingDependencyException</code>.
     */
    public void canonicalMethods();

    /**
     * Initialize <code>ct</code>'s fields.
     * This method ensures the list of fields is populated with
     * canonical FieldInstances, or throws a <code>MissingDependencyException</code>.
     */
    public void canonicalFields();

    /**
     * Initialize <code>ct</code>'s constructors.
     * This method ensures the list of fields is populated with (possibly
     * non-canonical) ConstructorInstances, or throws a <code>MissingDependencyException</code>.
     */
    public void initConstructors();

    /**
     * Initialize <code>ct</code>'s methods.
     * This method ensures the list of fields is populated with (possibly
     * non-canonical) MethodInstances, or throws a <code>MissingDependencyException</code>.
     */
    public void initMethods();

    /**
     * Initialize <code>ct</code>'s fields.
     * This method ensures the list of fields is populated with (possibly
     * non-canonical) FieldInstances, or throws a <code>MissingDependencyException</code>.
     */
    public void initFields();

    /**
     * Initialize <code>ct</code>'s member classes.
     * This method ensures the member classes of the class are initailized to
     * canonical types, or throws a <code>MissingDependencyException</code>.
     */
    public void initMemberClasses();

    /**
     * Initialize <code>ct</code>'s interfaces.
     * This method ensures the interfaces of the class are initailized to
     * canonical types, or throws a <code>MissingDependencyException</code>.
     */
    public void initInterfaces();
}
