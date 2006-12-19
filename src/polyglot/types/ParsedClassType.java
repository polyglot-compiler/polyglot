/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.types;

import java.util.*;

import polyglot.frontend.*;
import polyglot.frontend.Job;
import polyglot.frontend.Source;
import polyglot.util.Position;

/**
 * A <code>ParsedClassType</code> represents a class loaded from a source file.
 * <code>ParsedClassType</code>s are mutable.
 */
public interface ParsedClassType extends ClassType, ParsedTypeObject
{
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
    void addInterface(Type t);

    /**
     * Set the class's interfaces.
     */
    void setInterfaces(List l);
    
    /**
     * Add a field to the class.
     */
    void addField(FieldInstance fi);

    /**
     * Set the class's fields.
     */
    void setFields(List l);
    
    /**
     * Add a method to the class.
     */
    void addMethod(MethodInstance mi);

    /**
     * Set the class's methods.
     */
    void setMethods(List l);

    /**
     * Add a constructor to the class.
     */
    void addConstructor(ConstructorInstance ci);

    /**
     * Set the class's constructors.
     */
    void setConstructors(List l);
    
    /**
     * Add a member class to the class.
     */
    void addMemberClass(ClassType t);

    /**
     * Set the class's member classes.
     */
    void setMemberClasses(List l);

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
