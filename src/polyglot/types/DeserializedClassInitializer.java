package polyglot.types;

import polyglot.frontend.*;
import polyglot.frontend.goals.*;
import polyglot.types.*;
import polyglot.util.InternalCompilerError;
import java.util.Iterator;

/**
 * A LazyClassInitializer is responsible for initializing members of a class
 * after it has been created. Members are initialized lazily to correctly handle
 * cyclic dependencies between classes.
 */
public class DeserializedClassInitializer implements LazyClassInitializer {
    protected TypeSystem ts;
    protected ParsedClassType ct;
    protected boolean init;
    
    public DeserializedClassInitializer(TypeSystem ts) {
        this.ts = ts;
    }
    
    public void setClass(ParsedClassType ct) {
        this.ct = ct;
    }

    public boolean fromClassFile() {
        return false;
    }

    public void initTypeObject() {
        if (ct.isMember() && ct.outer() instanceof ParsedClassType) {
            ParsedClassType outer = (ParsedClassType) ct.outer();
            outer.addMemberClass(ct);
        }
        for (Iterator i = ct.memberClasses().iterator(); i.hasNext(); ) {
            ParsedClassType ct = (ParsedClassType) i.next();
            ct.initializer().initTypeObject();
        }
        this.init = true;
    }

    public boolean isTypeObjectInitialized() {
        return this.init;
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
