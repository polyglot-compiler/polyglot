/*
 * ForwardReferencesChecked.java
 * 
 * Author: nystrom
 * Creation date: Dec 19, 2004
 */
package polyglot.frontend.goals;

import polyglot.ast.NodeFactory;
import polyglot.frontend.*;
import polyglot.types.TypeSystem;
import polyglot.visit.FwdReferenceChecker;


public class ForwardReferencesChecked extends SourceFileGoal {
    public ForwardReferencesChecked(Job job) { super(job); }
    
    public Pass createPass(ExtensionInfo extInfo) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        return new VisitorPass(this, new FwdReferenceChecker(this, ts, nf));
    }
}