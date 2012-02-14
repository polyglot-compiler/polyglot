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

import polyglot.frontend.Scheduler;
import polyglot.frontend.goals.FieldConstantsChecked;
import polyglot.types.*;
import polyglot.util.*;

/**
 * A <code>VarInstance</code> contains type information for a variable.  It may
 * be either a local or a field.
 */
public abstract class VarInstance_c extends TypeObject_c implements VarInstance
{
    protected Flags flags;
    protected Type type;
    protected String name;
    protected Object constantValue;
    protected boolean isConstant;
    protected boolean constantValueSet;

    /** Used for deserializing types. */
    protected VarInstance_c() { }

    public VarInstance_c(TypeSystem ts, Position pos,
	                 Flags flags, Type type, String name) {
        super(ts, pos);
	this.flags = flags;
	this.type = type;
	this.name = name;
    this.decl = this;
    }
    
    protected VarInstance decl;
    
    public Declaration declaration() {
        return decl;
    }
    
    public void setDeclaration(Declaration decl) {
        this.decl = (VarInstance) decl;        
    }
    
    public boolean constantValueSet() {
        if (this != declaration()) {
            return ((VarInstance) declaration()).constantValueSet();
        }
        
        return constantValueSet;
    }
    
    public boolean isConstant() {
        if (this != declaration()) {
            return ((VarInstance) declaration()).isConstant();
        }
        
        if (! constantValueSet) {
            if (! flags.isFinal()) {
                setNotConstant();
                return isConstant;
            }
//            Scheduler scheduler = typeSystem().extensionInfo().scheduler();
//            scheduler.addConcurrentDependency(scheduler.currentGoal(), new ConstantsChecked(this));
        }
        return isConstant;
    }

    public Object constantValue() {
        if (this != declaration()) {
            return ((VarInstance) declaration()).constantValue();
        }

        if (isConstant()) {
            return constantValue;
        }
        return null;
    }

    public Flags flags() {
        return flags;
    }
    
    public Type type() {
        return type;
    }

    public String name() {
        return name;
    }

    public int hashCode() {
        return flags.hashCode() + name.hashCode();
    }

    public boolean equalsImpl(TypeObject o) {
        if (o instanceof VarInstance) {
	    VarInstance i = (VarInstance) o;
	    return flags.equals(i.flags())
	        && ts.equals(type, i.type())
		&& name.equals(i.name());
	}

	return false;
    }

    public boolean isCanonical() {
	return true;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setFlags(Flags flags) {
        this.flags = flags;
    }
    
    /** Destructive update of constant value. */
    public void setConstantValue(Object constantValue) {
        if (! (constantValue == null) &&
                ! (constantValue instanceof Boolean) &&
                ! (constantValue instanceof Number) &&
                ! (constantValue instanceof Character) &&
                ! (constantValue instanceof String)) {
            
            throw new InternalCompilerError(
            "Can only set constant value to a primitive or String.");
        }

        this.constantValue = constantValue;
        this.isConstant = true;
        this.constantValueSet = true;
    }
    
    public void setNotConstant() {
        this.constantValue = null;
        this.isConstant = false;
        this.constantValueSet = true;
    }
    
    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }
}
