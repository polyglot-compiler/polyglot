/*
 * ExceptionsChecked.java
 * 
 * Author: nystrom
 * Creation date: Dec 19, 2004
 */
package polyglot.frontend.goals;

import polyglot.ast.NodeFactory;
import polyglot.frontend.*;
import polyglot.types.TypeSystem;
import polyglot.visit.ExceptionChecker;


public class ExceptionsChecked extends SourceFileGoal {
    public ExceptionsChecked(Job job) { super(job); }
    
    public Pass createPass(polyglot.frontend.ExtensionInfo extInfo) {
        TypeSystem ts = extInfo.typeSystem();
        NodeFactory nf = extInfo.nodeFactory();
        return new VisitorPass(this, new ExceptionChecker(this, ts, nf));
    }
}