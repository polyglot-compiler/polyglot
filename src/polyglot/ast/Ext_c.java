package jltools.ext.jl.ast;

import jltools.ast.*;
import jltools.types.*;
import jltools.visit.*;
import jltools.util.*;

/**
 * <code>Ext</code> is the super type of all node extension objects.
 * It defines default implementations of the methods which implement compiler
 * passes, dispatching to the node to perform the actual work of the pass.
 */
public abstract class Ext_c implements Ext {
    protected Node node;

    public Ext_c() {
        this.node = null;
    }

    public void init(Node node) {
        if (this.node != null) {
            throw new InternalCompilerError("Already initialized.");
        }
        this.node = node;
    }

    /**
     * Return the node we ultimately extend, possibly this.
     */
    public Node node() {
	return node;
    }

    /**
     * Copy the extension.
     */
    public Object copy() {
        try {
            Ext_c copy = (Ext_c) super.clone();
            copy.node = null; // uninitialize
            return copy;
        }
        catch (CloneNotSupportedException e) {
            throw new InternalCompilerError("Java clone() weirdness.");
        }
    }

    public String toString() {
        return StringUtil.getShortNameComponent(getClass().getName());
    }
}
