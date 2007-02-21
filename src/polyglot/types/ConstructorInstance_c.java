/*
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2006 Polyglot project group, Cornell University
 * 
 */

package polyglot.types;

import polyglot.types.*;
import polyglot.util.*;
import java.util.*;

/**
 * A <code>ConstructorInstance</code> contains type information for a
 * constructor.
 */
public class ConstructorInstance_c extends ProcedureInstance_c
                                implements ConstructorInstance
{
    /** Used for deserializing types. */
    protected ConstructorInstance_c() { }

    public ConstructorInstance_c(TypeSystem ts, Position pos,
	                         ClassType container,
				 Flags flags, List formalTypes, List excTypes) {
        super(ts, pos, container, flags, formalTypes, excTypes);
        this.decl = this;
    }

    protected ConstructorInstance decl;
    
    public Declaration declaration() {
        return decl;
    }
    
    public void setDeclaration(Declaration decl) {
        this.decl = (ConstructorInstance) decl;        
    }
    
    public ConstructorInstance orig() {
        return (ConstructorInstance) declaration();
    }
    
    public ConstructorInstance flags(Flags flags) {
        if (!flags.equals(this.flags)) {
            ConstructorInstance_c n = (ConstructorInstance_c) copy();
            n.setFlags(flags);
            return n;
        }
        return this;
    }

    public ConstructorInstance formalTypes(List l) {
        if (!CollectionUtil.equals(this.formalTypes, l)) {
            ConstructorInstance_c n = (ConstructorInstance_c) copy();
            n.setFormalTypes(l);
            return n;
        }
        return this;
    }

    public ConstructorInstance throwTypes(List l) {
        if (!CollectionUtil.equals(this.throwTypes, l)) {
            ConstructorInstance_c n = (ConstructorInstance_c) copy();
            n.setThrowTypes(l);
            return n;
        }
        return this;
    }

    public ConstructorInstance container(ClassType container) {
        if (this.container != container) {
            ConstructorInstance_c n = (ConstructorInstance_c) copy();
            n.setContainer(container);
            return n;
        }
        return this;
    }

    public String toString() {
	return designator() + " " + flags.translate() + signature();
    }
    
    public String signature() {
        return container + "(" + TypeSystem_c.listToString(formalTypes) + ")";
    }

    public String designator() {
        return "constructor";
    }

    public boolean equalsImpl(TypeObject o) {
        if (o instanceof ConstructorInstance) {
            ConstructorInstance i = (ConstructorInstance) o;
            return ts.equals(container, i.container())
                && super.equalsImpl(i);
        }

        return false;
    }

    public boolean isCanonical() {
	return container.isCanonical()
	    && listIsCanonical(formalTypes)
	    && listIsCanonical(throwTypes);
    }
}
