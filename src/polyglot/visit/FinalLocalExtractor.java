/*
 * FinalLocalExtractor.java
 * 
 * Author: nystrom
 * Creation date: Apr 22, 2005
 */
package polyglot.visit;

import java.util.HashSet;
import java.util.Set;

import polyglot.ast.*;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.frontend.Job;
import polyglot.types.LocalInstance;
import polyglot.types.TypeSystem;

/**
 * This visitor converts non-final local variables into final local variables.
 * This improves the precision of some analyses.
 *
 * @author nystrom
 */
public class FinalLocalExtractor extends NodeVisitor {

    /** Set of LocalInstances declared final; these should not be made non-final. */
    Set isFinal;
    
    /**
     * @param job
     * @param ts
     * @param nf
     */
    public FinalLocalExtractor(Job job, TypeSystem ts, NodeFactory nf) {
        super();
    }

    public NodeVisitor begin() {
        isFinal = new HashSet();
        return super.begin();
    }
    
    public void finish() {
        isFinal = null;
    }
    
    // TODO: handle locals that are not initialized when declared
    //
    // TODO: handle anonymous classes: this visitor assumes all LocalInstances
    // are set correctly, which is true after disambiguation, except for anonymous
    // classes.
    //
    // TODO: convert to pseudo-SSA form: generate a new local decl when a local
    // is assigned, rather than marking the original as final.  If a local
    // requires a phi-function, just mark it non-final rather than generating
    // the phi.
    public NodeVisitor enter(Node parent, Node n) {
        if (n instanceof Formal) {
            Formal d = (Formal) n;
            LocalInstance li = d.localInstance();
            if (! li.flags().isFinal()) {
                li.setFlags(li.flags().Final());
            }
            else {
                isFinal.add(li);
            }
        }
        if (n instanceof LocalDecl) {
            LocalDecl d = (LocalDecl) n;
            LocalInstance li = d.localInstance();
            if (! li.flags().isFinal()) {
                li.setFlags(li.flags().Final());
            }
            else {
                isFinal.add(li);
            }
        }
        if (n instanceof Unary) {
            Unary u = (Unary) n;
            if (u.expr() instanceof Local) {
                Local l = (Local) u.expr();
                LocalInstance li = (LocalInstance) l.localInstance();
                if (u.operator() == Unary.PRE_DEC || u.operator() == Unary.POST_DEC ||
                    u.operator() == Unary.PRE_INC || u.operator() == Unary.POST_INC) {
                    if (! isFinal.contains(li)) {
                        li.setFlags(li.flags().clearFinal());
                    }
                }
            }
        }
        if (n instanceof Assign) {
            Assign a = (Assign) n;
            if (a.left() instanceof Local) {
                LocalInstance li = ((Local) a.left()).localInstance();
                if (! isFinal.contains(li)) {
                    li.setFlags(li.flags().clearFinal());
                }
            }
        }
        return super.enter(parent, n);
    }
    
    public Node leave(Node old, Node n, NodeVisitor v) {
        // Revisit everything to ensure the local decls' flags agree with
        // their local instance's.
        if (n instanceof SourceFile) {
            return n.visit(new NodeVisitor() {
                public Node leave(Node old, Node n, NodeVisitor v) {
                    if (n instanceof Formal) {
                        Formal d = (Formal) n;
                        return d.flags(d.localInstance().flags());
                    }
                    if (n instanceof LocalDecl) {
                        LocalDecl d = (LocalDecl) n;
                        return d.flags(d.localInstance().flags());
                    }
                    return n;
                }
            });
        }
        return n;
    }
}
