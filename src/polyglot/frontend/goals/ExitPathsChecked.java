/*
 * ExitPathsChecked.java
 * 
 * Author: nystrom
 * Creation date: Dec 19, 2004
 */
package polyglot.frontend.goals;

import polyglot.ast.NodeFactory;
import polyglot.frontend.*;
import polyglot.types.TypeSystem;
import polyglot.visit.ExitChecker;


public class ExitPathsChecked extends SourceFileGoal {
    public ExitPathsChecked(Job job) { super(job); }
    
    public Pass createPass(ExtensionInfo extInfo) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        return new VisitorPass(this, new ExitChecker(this, ts, nf));
    }
}