/*
 * ConstructorCallsChecked.java
 * 
 * Author: nystrom
 * Creation date: Dec 19, 2004
 */
package polyglot.frontend.goals;

import polyglot.ast.NodeFactory;
import polyglot.frontend.*;
import polyglot.types.TypeSystem;
import polyglot.visit.ConstructorCallChecker;


public class ConstructorCallsChecked extends SourceFileGoal {
    public ConstructorCallsChecked(Job job) { super(job); }
    
    public Pass createPass(ExtensionInfo extInfo) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        return new VisitorPass(this, new ConstructorCallChecker(this, ts, nf));
    }
}