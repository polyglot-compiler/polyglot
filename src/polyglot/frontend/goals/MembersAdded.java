/*
 * MembersAdded.java
 * 
 * Author: nystrom
 * Creation date: Dec 15, 2004
 */
package polyglot.frontend.goals;

import polyglot.ast.NodeFactory;
import polyglot.frontend.*;
import polyglot.frontend.passes.AddMembersPass;
import polyglot.types.ParsedClassType;
import polyglot.types.TypeSystem;
import polyglot.visit.TypeBuilder;

/**
 * Comment for <code>MembersAdded</code>
 *
 * @author nystrom
 */
public class MembersAdded extends ClassTypeGoal {
    public MembersAdded(ParsedClassType ct) {
        super(ct);
    }

    public Pass createPass(ExtensionInfo extInfo) {
        if (job() != null) {
            TypeSystem ts = extInfo.typeSystem();
            NodeFactory nf = extInfo.nodeFactory();
            return new VisitorPass(this, new TypeBuilder(this, ts, nf));
        }
        return new AddMembersPass(extInfo.scheduler(), this);
    }
    
    public boolean reached() {
        return type().membersAdded();
    }
    
    public boolean equals(Object o) {
        return o instanceof MembersAdded && super.equals(o);
    }
}
