/*
 * SuperTypesResolved.java
 * 
 * Author: nystrom
 * Creation date: Dec 15, 2004
 */
package polyglot.frontend.goals;

import polyglot.ast.NodeFactory;
import polyglot.frontend.*;
import polyglot.frontend.passes.ResolveSuperTypesPass;
import polyglot.frontend.passes.TypeCheckPass;
import polyglot.types.ParsedClassType;
import polyglot.types.TypeSystem;
import polyglot.visit.TypeChecker;

/**
 * Comment for <code>SuperTypesResolved</code>
 *
 * @author nystrom
 */
public class SupertypesResolved extends ClassTypeGoal {
    public SupertypesResolved(ParsedClassType ct) {
        super(ct);
    }
    
    public Pass createPass(ExtensionInfo extInfo) {
        if (job() != null) {
            TypeSystem ts = extInfo.typeSystem();
            NodeFactory nf = extInfo.nodeFactory();
            return new TypeCheckPass(this, new TypeChecker(this, ts, nf));
        }
        return new ResolveSuperTypesPass(extInfo.scheduler(), this);
    }
    
    public boolean reached() {
        return type().supertypesResolved();
    }

    public boolean equals(Object o) {
        return o instanceof SupertypesResolved && super.equals(o);
    }
}
