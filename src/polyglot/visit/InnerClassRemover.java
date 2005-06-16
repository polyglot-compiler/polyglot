/*
 * Created on May 18, 2005
 */
package polyglot.visit;

import polyglot.ast.*;
import polyglot.frontend.Job;
import polyglot.types.*;

/**
 * @author nystrom
 *
 * This class translates inner classes to static nested classes with a field
 * pointing to the enclosing instance.
 */
public class InnerClassRemover extends ContextVisitor
{
    public InnerClassRemover(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
    }
    
    protected Node leaveCall(Node old, Node n, NodeVisitor v) throws SemanticException {
        if (n instanceof ClassDecl) {
            ClassDecl cd = (ClassDecl) n;
            
            ParsedClassType ct = cd.type();
            
            if (ct.isMember() && ! ct.flags().isStatic()) {
                ct.flags(ct.flags().Static());
                cd = cd.flags(ct.flags());
            }
            
            n = cd;
        }
        
        n = super.leaveCall(old, n, v);
        return n;
    }
}
