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

import polyglot.frontend.Job;
import polyglot.frontend.Source;
import polyglot.util.Position;

/**
 * A <code>ParsedClassType</code> represents a class loaded from a source file.
 * <code>ParsedClassType</code>s are mutable.
 */
public interface ParsedClassType extends ClassType, ParsedTypeObject {
    void setJob(Job job);

    /**
     * Position of the type's declaration.
     */
    void position(Position pos);

    /**
     * The <code>Source</code> that this class type
     * was loaded from. Should be <code>null</code> if it was not loaded from
     * a <code>Source</code> during this compilation. 
     */
    Source fromSource();

    /**
     * Set the class's package.
     */
    void package_(Package p);

    /**
     * Set the class's super type.
     */
    void superType(Type t);

    /**
     * Add an interface to the class.
     */
    void addInterface(ReferenceType t);

    /**
     * Set the class's interfaces.
     */
    void setInterfaces(List<? extends ReferenceType> l);

    /**
     * Add a field to the class.
     */
    void addField(FieldInstance fi);

    /**
     * Set the class's fields.
     */
    void setFields(List<? extends FieldInstance> l);

    /**
     * Add a method to the class.
     */
    void addMethod(MethodInstance mi);

    /**
     * Set the class's methods.
     */
    void setMethods(List<? extends MethodInstance> l);

    /**
     * Add a constructor to the class.
     */
    void addConstructor(ConstructorInstance ci);

    /**
     * Set the class's constructors.
     */
    void setConstructors(List<? extends ConstructorInstance> l);

    /**
     * Add a member class to the class.
     */
    void addMemberClass(ClassType t);

    /**
     * Set the class's member classes.
     */
    void setMemberClasses(List<? extends ClassType> l);

    /**
     * Set the flags of the class.
     */
    void flags(Flags flags);

    /**
     * Set the class's outer class.
     */
    void outer(ClassType t);

    /**
     * Set the name of the class.  Throws <code>InternalCompilerError</code>
     * if called on an anonymous class.
     */
    void name(String name);

    /**
     * Set the class's kind.
     */
    void kind(Kind kind);

    /**
     * Set whether the class was declared in a static context.
     */
    void inStaticContext(boolean inStaticContext);

    boolean defaultConstructorNeeded();

    boolean membersAdded();

    boolean supertypesResolved();

    boolean signaturesResolved();

    int numSignaturesUnresolved();

    void setMembersAdded(boolean flag);

    void setSupertypesResolved(boolean flag);

    void setSignaturesResolved(boolean flag);

    boolean needSerialization();

    void needSerialization(boolean b);
}
