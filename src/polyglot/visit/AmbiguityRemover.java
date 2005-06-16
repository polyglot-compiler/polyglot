package polyglot.visit;

import java.util.Arrays;
import java.util.Collection;

import polyglot.ast.*;
import polyglot.frontend.Job;
import polyglot.main.Report;
import polyglot.types.SemanticException;
import polyglot.types.TypeSystem;
import polyglot.types.UnavailableTypeException;
import polyglot.util.ErrorInfo;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;

/**
 * A visitor which traverses the AST and remove ambiguities found in fields,
 * method signatures and the code itself.
 */
public class AmbiguityRemover extends DisambiguationDriver
{
    boolean visitSigs;
    boolean visitBodies;
    
    public AmbiguityRemover(Job job, TypeSystem ts, NodeFactory nf) {
        this(job, ts, nf, true, true);
    }
    
    public AmbiguityRemover(Job job, TypeSystem ts, NodeFactory nf, boolean visitSigs, boolean visitBodies) {
        super(job, ts, nf);
        this.visitSigs = visitSigs;
        this.visitBodies = visitBodies;
    }

    public Node override(Node parent, Node n) {
        if (! visitSigs && n instanceof ClassMember && ! (n instanceof ClassDecl)) {
            return n;
        }
        if ((! visitBodies || ! visitSigs) && (n instanceof Expr || n instanceof Stmt)) {
            return n;
        }
        
        try {
            if (Report.should_report(Report.visit, 2))
                Report.report(2, ">> " + this + "::override " + n);
            
            Node m = n.del().disambiguateOverride(parent, this);
            
            if (Report.should_report(Report.visit, 2))
                Report.report(2, "<< " + this + "::override " + n + " -> " + m);
            
            return m;
        }
        catch (SemanticException e) {
            if (e.getMessage() != null) {
                Position position = e.position();
                
                if (position == null) {
                    position = n.position();
                }
                
                this.errorQueue().enqueue(ErrorInfo.SEMANTIC_ERROR,
                                     e.getMessage(), position);
            }
            else {
                // silent error; these should be thrown only
                // when the error has already been reported 
            }
            
            return n;
        }    
    }
    
    protected NodeVisitor enterCall(Node n) throws SemanticException {
        if (Report.should_report(Report.visit, 2))
            Report.report(2, ">> " + this + "::enter " + n);
        
        AmbiguityRemover v = (AmbiguityRemover) n.del().disambiguateEnter(this);
        
        if (Report.should_report(Report.visit, 2))
            Report.report(2, "<< " + this + "::enter " + n + " -> " + v);
        
        return v;
    }
    
    protected Node leaveCall(Node old, Node n, NodeVisitor v) throws SemanticException {
        if (Report.should_report(Report.visit, 2))
            Report.report(2, ">> " + this + "::leave " + n);

        final boolean[] amb = new boolean[1];
        
        n.visitChildren(new NodeVisitor() {
            public Node override(Node n) {
                if (n instanceof Ambiguous) {
                    amb[0] = true;
                }
                return n;
            }
        });
        
        Node m = n;
        
        try {
            if (! amb[0]) {
                m = m.del().disambiguate((AmbiguityRemover) v);
            }
        }
        catch (UnavailableTypeException e) {
            // ignore: we'll rerun the pass later
        }
        
        if (Report.should_report(Report.visit, 2))
            Report.report(2, "<< " + this + "::leave " + n + " -> " + m);
        
        return m;
    }
    
    public HaltingVisitor bypass(Collection c) {
        throw new InternalCompilerError("AmbiguityRemover does not support bypassing. " +
                                        "Implement any required functionality using " +
                                        "Node.disambiguateOverride(Node, AmbiguityRemover).");
    }
    public HaltingVisitor bypass(Node n) {
        throw new InternalCompilerError("AmbiguityRemover does not support bypassing. " +
                                        "Implement any required functionality using " +
                                        "Node.disambiguateOverride(Node, AmbiguityRemover).");
    }
    public HaltingVisitor bypassChildren(Node n) {
        throw new InternalCompilerError("AmbiguityRemover does not support bypassing. " +
                                        "Implement any required functionality using " +
                                        "Node.disambiguateOverride(Node, AmbiguityRemover).");
    }
  
    public boolean isASTDisambiguated(Node n) {
        return astAmbiguityCount(n) == 0;
    }
    
    public static int astAmbiguityCount(Node n) {
        final Collection TOPICS = Arrays.asList(new String[] { Report.types, Report.frontend, "disam-check" });

        final int[] notOkCount = new int[] { 0 };
        
        n.visit(new NodeVisitor() {
            public Node override(Node parent, Node n) {
                // Don't check if New is disambiguated; this is handled
                // during type-checking.
                if (n instanceof New) {
                    return n;
                }

                if (! n.isDisambiguated()) {
                    if (Report.should_report(TOPICS, 3))
                        Report.report(3, "  not ok at " + n + " (" + n.getClass().getName() + ")");
                    notOkCount[0]++;
                }
                
                return null;
            }
        });
        
        return notOkCount[0];
    }
}
