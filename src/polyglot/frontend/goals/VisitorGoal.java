/*
 * VisitorGoal.java
 * 
 * Author: nystrom
 * Creation date: Dec 19, 2004
 */
package polyglot.frontend.goals;

import polyglot.ast.NodeFactory;
import polyglot.frontend.*;
import polyglot.types.TypeSystem;
import polyglot.visit.NodeVisitor;
import polyglot.util.StringUtil;

public class VisitorGoal extends SourceFileGoal {
    NodeVisitor v;
    
    public VisitorGoal(Job job, NodeVisitor v) {
        super(job);
        this.v = v;
    }
    
    public Pass createPass(ExtensionInfo extInfo) {
        return new VisitorPass(this, v);
    }
    
    public NodeVisitor visitor() {
        return v;
    }
    
    public int hashCode() {
        return job().hashCode() + visitor().getClass().hashCode() + getClass().hashCode();
    }
    
    public boolean equals(Object o) {
        if (o instanceof VisitorGoal) {
            VisitorGoal g = (VisitorGoal) o;
            return job().equals(g.job()) && visitor().getClass() == g.visitor().getClass() && this.getClass() == o.getClass();
        }
        return false;
    }
    
    public String toString() {
        return job() + ":" +
            StringUtil.getShortNameComponent(getClass().getName()) +
            ":" + visitor() + " (" + stateString() + ")";
    }
}
