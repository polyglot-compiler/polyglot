package polyglot.ext.jl.ast;

import polyglot.ast.*;
import polyglot.types.*;
import polyglot.visit.*;
import polyglot.util.*;

/**
 * <code>Ext</code> is the super type of all node extension objects.
 * It defines default implementations of the methods which implement compiler
 * passes, dispatching to the node to perform the actual work of the pass.
 */
public abstract class Ext_c implements Ext {
    protected Node node;
    protected Ext ext;

    public Ext_c(Ext ext) {
        this.node = null;
        this.ext = ext;
    }

    public Ext_c() {
        this(null);
        this.node = null;
    }

    public void init(Node node) {
        if (this.node != null) {
            throw new InternalCompilerError("Already initialized.");
        }

        this.node = node;

        if (this.ext != null) {
            this.ext.init(node);
        }
    }

    /**
     * Return the node we ultimately extend, possibly this.
     */
    public Node node() {
	return node;
    }

    public Ext ext() {
        return ext;
    }

    public Ext ext(Ext ext) {
        Ext old = this.ext;
        this.ext = null;

        Ext_c copy = (Ext_c) copy();

        copy.ext = ext;

        this.ext = old;

        return copy;
    }

    /**
     * Copy the extension.
     */
    public Object copy() {
        try {
            Ext_c copy = (Ext_c) super.clone();
            if (ext != null) {
                copy.ext = (Ext) copy.copy();
            }
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
