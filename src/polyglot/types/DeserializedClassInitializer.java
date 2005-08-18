package polyglot.ext.jl.types;

import polyglot.frontend.*;
import polyglot.frontend.goals.*;
import polyglot.types.*;
import polyglot.util.InternalCompilerError;

/**
 * A LazyClassInitializer is responsible for initializing members of a class
 * after it has been created. Members are initialized lazily to correctly handle
 * cyclic dependencies between classes.
 */
public class DeserializedClassInitializer implements LazyClassInitializer {
    protected TypeSystem ts;
    protected ParsedClassType ct;
    
    public DeserializedClassInitializer(TypeSystem ts) {
        this.ts = ts;
    }
    
    public void setClass(ParsedClassType ct) {
        this.ct = ct;
    }

    public boolean fromClassFile() {
        return false;
    }

    public void initSuperclass() {
    }

    public void initInterfaces() {
    }

    public void initMemberClasses() {
    }

    public void initConstructors() {
    }

    public void initMethods() {
    }

    public void initFields() {
    }

    public void canonicalConstructors() {
    }

    public void canonicalMethods() {
    }

    public void canonicalFields() {
    }
}
