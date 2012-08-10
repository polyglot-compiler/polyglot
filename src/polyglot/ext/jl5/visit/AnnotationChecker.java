package polyglot.ext.jl5.visit;

import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.ext.jl5.ast.AnnotatedElement;
import polyglot.frontend.Job;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.visit.ContextVisitor;
import polyglot.visit.NodeVisitor;

/** 
 *
 * Visitor that checks annotations of annotated elements 
 *
 */
public class AnnotationChecker extends ContextVisitor {
    public AnnotationChecker(Job job, TypeSystem ts, NodeFactory nf) {
        super(job, ts, nf);
    }

    @Override
    protected Node leaveCall(Node old, Node n, NodeVisitor v)
            throws SemanticException {
        if (n instanceof AnnotatedElement) {
            AnnotatedElement ae = (AnnotatedElement) n;
            return ae.annotationCheck((AnnotationChecker) v);
        }
        return n;
    }
}
